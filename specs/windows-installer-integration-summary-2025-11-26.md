# Windows Installer Integration – Work Summary (2025-11-26)

This document summarizes all changes implemented to enable local and CI builds of a Windows installer for Woodlanders Launcher, including uninstaller support, versioning, and release notes.

## Overview
- Consolidated Gradle and scripts around a single app module (`:app`).
- Standardized launcher JAR name to a stable `woodlanders-launcher.jar`.
- Implemented a local builder to generate an installer EXE and uninstaller EXE.
- Updated GitHub Actions workflow to build, package, and publish installer artifacts and static release notes.
- Hardened the installer template to handle JavaFX availability and JRE auto-download.
- Updated documentation to reflect the new install/uninstall flow.

---

## Gradle / Build Output
- File: `app/build.gradle`
  - JAR task produces a non-versioned file:
    - `archiveBaseName = 'woodlanders-launcher'`
    - `archiveVersion = ''` → outputs `app/build/libs/woodlanders-launcher.jar` (no version suffix)
  - Existing distribution tasks retained; only JAR naming adjusted.

- Verified output location:
  - `app/build/libs/woodlanders-launcher.jar`

---

## Local Builder Script
- File: `windows/build-launcher-installer-local.ps1`
  - Build: `gradle :app:jar -x test --no-daemon`.
  - JAR staging: prefers `app\build\libs\woodlanders-launcher.jar`, falls back to `woodlanders-launcher-*.jar`.
  - Icons: copies from `icon\icon.ico` (or `icon.png`).
  - Installer script: materializes from `.github\scripts\windows-installer-template.ps1` with `${VERSION}` → `LOCAL-BUILD`.
  - EXE creation: converts `installer-staging\install.ps1` to `build\distributions\woodlanders-setup-launcher.exe` via `ps2exe`.
  - Uninstaller: converts `windows\uninstall.ps1` to `build\distributions\woodlanders-uninstall.exe` and copies `uninstall.ps1`.
  - ZIP packaging: creates `build\distributions\woodlanders-launcher-installer.zip` with:
    - `woodlanders-setup-launcher.exe`
    - `woodlanders-uninstall.exe`
    - `woodlanders-launcher.jar`
    - `launcher.ico` (if present)
    - `install.ps1`
    - `uninstall.ps1`
    - `README.txt`
  - Summary output updated to list uninstaller files.

---

## GitHub Actions Workflow
- File: `.github/workflows/build-launcher-installer.yml`
  - Build: `gradle :app:jar -x test --no-daemon`.
  - Staging:
    - Copy JAR from `app\build\libs` (prefers non-versioned, falls back to `*-versioned`).
    - Copy icon from `icon\icon.ico` (or `.png`).
  - Installer script generation:
    - Reads `.github\scripts\windows-installer-template.ps1`.
    - Replaces `${VERSION}` with `${{ github.ref_name }}`.
    - Saves as `installer-staging\install.ps1`.
  - ps2exe conversions:
    - `installer-staging\install.ps1` → `build\distributions\woodlanders-setup-launcher.exe`.
    - `windows\uninstall.ps1` → `build\distributions\woodlanders-uninstall.exe`.
  - Packaging/Artifact:
    - Copies staged files into `build\distributions\`.
    - Zips the following into `woodlanders-launcher-installer.zip`:
      - `woodlanders-setup-launcher.exe`
      - `woodlanders-uninstall.exe`
      - `woodlanders-launcher.jar`
      - `launcher.ico`
      - `install.ps1`
      - `uninstall.ps1`
      - `README.txt`
  - Release notes:
    - Generates `build\distributions\RELEASE_NOTES.md` with static content.
    - Uses `body_path: build/distributions/RELEASE_NOTES.md` in `softprops/action-gh-release`.
  - Uploads ZIP to the tag’s GitHub Release and as workflow artifact.

---

## Installer Template
- File: `.github/scripts/windows-installer-template.ps1`
  - `$APP_VERSION = "${VERSION}"` (replaced by workflow with tag/ref name).
  - JavaFX detection: `Test-JavaFxAvailable` checks `java --list-modules` for `javafx.controls`.
  - JRE fallback: downloads Liberica Full JDK 21 (with JavaFX) when system Java lacks JavaFX or Java ≥17 is missing.
  - Launcher script emits a `.bat` that sets `--add-modules=javafx.controls` and ensures JavaFX cache dir exists.
  - Robust `$scriptDir` detection for both PS1 and ps2exe EXE execution contexts.

---

## Uninstaller
- File: `windows/uninstall.ps1`
  - Removes installation directory, desktop shortcut, Start Menu entry, and JavaFX cache.
  - Converted to `woodlanders-uninstall.exe` in both local script and CI workflow and included in ZIP.

---

## Documentation Updates
- File: `README.md`
  - Windows install: use `woodlanders-setup-launcher.exe` (or `install.ps1`) from the latest release ZIP.
  - Windows uninstall: use `woodlanders-uninstall.exe` (or `uninstall.ps1`), with manual cleanup fallback.
  - Removed legacy references to `INSTALL.bat` and versioned ZIP names.

- File: `docs/WINDOWS_INSTALL_GUIDE.md`
  - Updated steps to use `woodlanders-setup-launcher.exe` or `install.ps1`.
  - Uninstall steps include `woodlanders-uninstall.exe` or `uninstall.ps1`, plus manual fallback.
  - Links point to Releases page instead of hardcoded versioned ZIP names.

---

## Release Notes Content
- Static release notes are embedded by the workflow into the GitHub Release body via `RELEASE_NOTES.md`.
- Notes emphasize early alpha status, update cadence, and Quick Start (with Snap badge link).

---

## How to Run Locally
```powershell
# Build the JAR
gradle :app:jar -x test --no-daemon

# Build the installer EXE + ZIP bundle
pwsh -ExecutionPolicy Bypass -File windows\build-launcher-installer-local.ps1

# Outputs
#  build\distributions\woodlanders-setup-launcher.exe
#  build\distributions\woodlanders-uninstall.exe
#  build\distributions\woodlanders-launcher-installer.zip
```

---

## Notes / Future Enhancements
- Code signing (Publisher name in Windows prompts): requires signing `*.exe` with a code-signing certificate; optional workflow steps can be added.
- Version centralization: consider a single `appVersion` in `gradle.properties` to drive docs and packaging.
- JRE URL maintenance: option to derive download URL from `$JRE_VERSION` or add checksum verification.
