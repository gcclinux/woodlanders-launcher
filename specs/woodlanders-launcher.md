# Woodlanders Launcher & Installer - Project Plan

## 1. Overview
A graphical + CLI-capable launcher for the Woodlanders game distributed as a Snap package on Ubuntu. It ensures the local cached Java client `woodlanders-client.jar` in `${HOME}/.config/woodlanders/` matches the latest release asset from `gcclinux/Woodlanders` GitHub repository. Provides a single central button whose label and action adapt to state: `Launch` (up-to-date) or `Download / Update` (stale or missing). After ensuring freshness, it executes: `java -jar ${HOME}/.config/woodlanders/woodlanders-client.jar`.

## 2. Objectives
- Simple frictionless install via `snap install woodlanders-launcher`.
- Auto-detection & retrieval of latest release asset.
- Safe, atomic updates (avoid partial/corrupt JARs).
- Minimal, focused UI (one primary action, optional details panel/log).
- Works offline (graceful degradation; allow launching existing JAR if present even if version check fails).
- Provide clear error messages (missing Java runtime, network failure, permissions).

## 3. User Flow
1. User launches the snap (`woodlanders-launcher`).
2. App initializes: ensure config directory `${HOME}/.config/woodlanders` exists.
3. Loads local metadata (version file) and checks presence of `woodlanders-client.jar`.
4. Queries GitHub API for latest release (`https://api.github.com/repos/gcclinux/Woodlanders/releases/latest`).
5. Compares `tag_name` with saved local version (from `${HOME}/.config/woodlanders/version.json`).
6. UI state:
   - Up-to-date: Show `Launch Woodlanders` button.
   - Missing or outdated: Show `Download Latest` button.
7. If download needed:
   - Fetch asset (find matching `.jar` asset, use `browser_download_url`).
   - Stream to temp file (e.g. `woodlanders-client.tmp`) with progress.
   - Verify size & (optional) SHA256.
   - Move temp file atomically to `woodlanders-client.jar`.
   - Update `version.json`.
8. Execute `java -jar ...` (spawn child process, detach; close launcher or keep small status window with a “Close” button).

## 4. Versioning Strategy
- Source of truth: GitHub release `tag_name`.
- Local metadata file: `${HOME}/.config/woodlanders/version.json` containing `{ "version": "vX.Y.Z", "downloaded_at": "ISO8601", "sha256": "..." }`.
- Comparison: If no file OR mismatch of `version` OR integrity check fails => update required.

## 5. GitHub Release Integration
- Endpoint: `GET /repos/gcclinux/Woodlanders/releases/latest`.
- Rate limiting: Minimal single request per app launch; cache timestamp to avoid repeated calls (optional optimization).
- Asset selection: Iterate `assets[]` for name matching `woodlanders-client.jar` OR pattern `*client*.jar`.
- Handling failures: If API errors, degrade to offline mode—allow launch if local JAR exists; else show retry button.

## 6. Local Storage Paths
```
CONFIG_DIR = ${HOME}/.config/woodlanders/
JAR_PATH   = ${CONFIG_DIR}/woodlanders-client.jar
VERSION_MD = ${CONFIG_DIR}/version.json
LOG_PATH   = ${CONFIG_DIR}/launcher.log (optional)
```

## 7. Launcher Behavior Logic (State Machine)
States:
- INIT -> CHECKING_REMOTE -> (REMOTE_FAILED | REMOTE_OK)
- REMOTE_OK + LOCAL_MATCH => READY_TO_LAUNCH
- REMOTE_OK + LOCAL_MISSING_OR_OLD => NEEDS_UPDATE
- REMOTE_FAILED + LOCAL_PRESENT => OFFLINE_LAUNCH_POSSIBLE
- REMOTE_FAILED + LOCAL_MISSING => BLOCKED (show retry)
Transitions triggered by network result, version compare, download completion.

## 8. Technology Choices
Primary implementation language: Java 17 (matched with OpenJDK LTS) packaged inside the snap for consistent runtime.
UI Toolkit: JavaFX 17 for native-feeling desktop UI, CSS styling, and straightforward snap bundling via modular runtime image.
HTTP/JSON Layer: Java 11+ `java.net.http.HttpClient` with Jackson (or Gson) for JSON parsing.
Build Tool: Gradle (Kotlin DSL) for flexible packaging, custom tasks, and easier integration with Snapcraft.
Alternatives (future consideration):
- Rust + egui (more complex packaging) 
- Electron (heavier footprint) 
- SWT + plain Java AWT (limited modern UI widgets)
Decision rationale: Java keeps stack aligned with the shipped JAR, simplifies calling `java -jar`, and enables reusable code for both CLI and GUI in a single runtime.

## 9. Snap Packaging Outline
`snapcraft.yaml` draft components:
```
name: woodlanders-launcher
base: core24
version: 0.1.0
summary: Woodlanders game launcher
description: Ensures you have the latest Woodlanders client and launches it.
confinement: strict
grade: stable
apps:
  launcher:
    command: bin/woodlanders-launcher
    plugs: [network, home, desktop, desktop-legacy, x11, wayland]
parts:
  launcher:
    plugin: gradle
    source: .
    gradle-output-dir: build/install/woodlanders-launcher
    build-snaps: [openjdk-17/edge]
    stage-packages: [openjdk-17-jre, libgtk-3-0]
```
Notes:
- `home` plug needed for `${HOME}/.config` access.
- Add `network` for API + downloads.
- OpenJDK staged so we guarantee Java presence and match JavaFX runtime.
- Command script `bin/woodlanders-launcher` will invoke the Gradle-installed launcher shell script (or custom wrapper) that starts the JavaFX app.

## 10. Security & Confinement
- Strict confinement with explicit plugs: only home + network + display.
- Validate downloaded asset length + optional SHA256 to mitigate tampering.
- Use HTTPS browser download URL; rely on GitHub TLS.
- Avoid executing arbitrary paths; only the verified JAR.

## 11. Version Check Algorithm (Detailed)
Pseudo:
```
ensure_config_dir()
local_version = read_version_file()  # may be None
latest = fetch_latest_release_json()
if latest is None:
    if jar_exists(): state = OFFLINE_LAUNCH_POSSIBLE else state = BLOCKED
else:
    latest_tag = latest['tag_name']
    jar_asset = select_asset(latest['assets'])
    if not jar_asset: state = ERROR_NO_ASSET
    else:
        if local_version == latest_tag and jar_exists(): state = READY_TO_LAUNCH
        else: state = NEEDS_UPDATE
```

## 12. Update / Download Procedure
1. Disable main button; show progress bar.
2. Stream download (chunked) to temp file.
3. Track bytes received / total for progress.
4. After completion: compute SHA256, update metadata file.
5. Atomic rename temp -> final.
6. Switch UI to `Launch` state.
7. Errors: show retry; preserve old JAR if still valid.

## 13. Java Runtime Handling
- On start: run `java -version` (subprocess) OR rely on internal path from staged OpenJDK.
- If missing (when not staged), show dialog: "Java runtime not found. Please install `openjdk-17-jre`." + disable `Launch`.
- If staging OpenJDK, skip check.

## 14. Configuration & Metadata
`version.json` example:
```
{
  "version": "v1.2.3",
  "sha256": "<hex>",
  "downloaded_at": "2025-11-23T12:34:56Z",
  "asset_size": 12345678
}
```
Fallback if missing: treat as outdated.

## 15. Logging Strategy
- Append structured lines to `launcher.log` (timestamp, level, event, context).
- Levels: INFO, WARN, ERROR.
- Useful for debugging user-reported issues with update / launch.

## 16. Error Handling & Offline Mode
- Network failure: Provide `Retry` and (if JAR present) `Launch Anyway`.
- Asset missing: Hard error; link to releases page.
- Corrupt download (size mismatch / SHA mismatch): Discard temp, show error, allow retry.
- Java missing: Provide instructions or stage runtime.

## 17. Minimal UI Specification
Window:
- Title: "Woodlanders Launcher"
- Center area: Large primary button (dynamic label: Launch / Download Latest / Retry / Launch Offline)
- Optional lower area: small expandable "Details" showing version info: `Local: vX.Y.Z | Latest: vA.B.C`.
- Progress: Replace button text with progress percentage during download or show a progress bar below.

## 18. CLI Fallback (Optional Initial Phase)
Allow running the tool as CLI (same snap command) with flags:
- `--quiet` (only exit codes) 
- `--force-update` (forces re-download) 
- `--print-status` (prints JSON describing local vs latest)

## 19. Future Enhancements
- Background auto-update on interval.
- Notification integration (desktop notifications when update available).
- Multi-platform packages (Flatpak, AppImage).
- Digital signature verification of JAR (if publisher provides signatures).
- Mirror fallback (if GitHub unreachable).

## 20. Implementation Milestones
1. Scaffold Gradle-based Java project (`app` module for launcher core, optional `cli` module if needed).
2. Implement version + path utilities (Java service classes + tests).
3. Implement GitHub API client (latest release fetch + asset select) using `HttpClient`.
4. Implement download manager (progress + integrity check) leveraging NIO streams.
5. Implement metadata persistence (JSON serialization via Jackson/Gson).
6. Build minimal JavaFX UI with dynamic button.
7. Integrate launch logic (spawn `ProcessBuilder` with `java -jar ...`).
8. Add logging & error dialogs (Slf4j + java.util.logging backend or Logback).
9. Add CLI argument parsing for headless usage (e.g., Picocli submodule).
10. Draft `snapcraft.yaml` and build initial snap.
11. Test confined access & adjust plugs.
12. QA: offline scenarios, corrupt downloads.
13. Prepare README & usage docs.

## 21. Risks & Mitigations
- Asset naming changes: Mitigate by pattern search + configurable regex.
- API rate limiting: Single call per launch; negligible risk.
- User without write perms on config dir: Detect & display explicit error.
- Java runtime mismatch: Bundle OpenJDK to reduce dependency friction.

## 22. Initial Module Outline (Java)
```
app/
  src/main/java/com/woodlanders/launcher/
    LauncherApp.java           # JavaFX entry point
    LauncherController.java    # UI logic
    LauncherState.java         # enum for UI states
    services/
      ConfigService.java       # path resolution, directory creation
      VersionService.java      # read/write version metadata
      GithubService.java       # latest release fetching
      DownloadService.java     # streaming download + checksum
      LaunchService.java       # spawn game process
    model/
      ReleaseInfo.java
      VersionMetadata.java
  src/main/resources/
    application.css            # styling placeholder

cli/ (optional future module using same services)

build.gradle.kts (root) defining application + packaging tasks
```
Entry command invoked by snap: `bin/woodlanders-launcher` -> `java -m com.woodlanders.launcher/com.woodlanders.launcher.LauncherApp`.

## 23. Snapcraft Build Notes
- Use `base: core24` for latest libs.
- If GTK requires additional stage packages (libgtk-3-0), include them.
- Validate that OpenJDK inclusion does not bloat excessively; consider using system Java if acceptable.

## 24. Success Criteria
- Fresh install: first launch downloads JAR automatically and launches game.
- Subsequent launch with unchanged upstream version: directly launches within <2s.
- Update cycle: When upstream release increments, launcher fetches new version reliably.
- Offline: Launches previously cached JAR without crashing.

## 25. Next Immediate Step
Proceed to implement scaffold + version check logic before UI, enabling quick CLI test of update flow.

---
End of initial project plan.
