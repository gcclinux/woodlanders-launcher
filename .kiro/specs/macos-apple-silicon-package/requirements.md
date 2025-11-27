# Requirements Document

## Introduction

This document specifies the requirements for creating a native macOS application package for the Woodlanders Launcher targeting Apple Silicon (M-series) processors. The package will provide the same functionality as existing Windows, Snap, and Flatpak packages: automatically checking for game updates, downloading the latest Woodlanders client, and launching the game with a single click. The macOS package will bundle the Java runtime (JDK 21) and JavaFX for arm64 architecture, ensuring users can run the launcher without additional dependencies.

## Glossary

- **Launcher**: The Woodlanders Launcher JavaFX application that manages game updates and launches
- **Apple Silicon**: ARM64-based processors (M1, M2, M3, M4 series) used in modern Mac computers
- **DMG**: Disk Image file format used for distributing macOS applications
- **App Bundle**: macOS application package structure (.app directory) containing executable, resources, and metadata
- **JDK**: Java Development Kit, includes the Java Runtime Environment (JRE) needed to run Java applications
- **JavaFX**: Java framework for building desktop GUI applications
- **arm64**: 64-bit ARM architecture used by Apple Silicon processors
- **Info.plist**: Property list file containing application metadata for macOS
- **Code Signing**: Process of digitally signing macOS applications for security and Gatekeeper compatibility
- **Gatekeeper**: macOS security feature that verifies application signatures before allowing execution
- **Gradle**: Build automation tool used by the Woodlanders Launcher project

## Requirements

### Requirement 1

**User Story:** As a macOS user with an Apple Silicon Mac, I want to download a single DMG file that contains everything needed to run the Woodlanders Launcher, so that I can install and use the launcher without installing Java separately.

#### Acceptance Criteria

1. WHEN the macOS package is built THEN the system SHALL produce a DMG file containing a complete .app bundle
2. WHEN the DMG is opened THEN the system SHALL display an installation window with the application icon and Applications folder shortcut
3. WHEN the user drags the app to Applications THEN the system SHALL copy the complete bundle including bundled JDK and JavaFX
4. WHERE the target system is Apple Silicon THEN the system SHALL include arm64-native JDK 21 binaries
5. WHERE the target system is Apple Silicon THEN the system SHALL include arm64-native JavaFX 21 libraries

### Requirement 2

**User Story:** As a macOS user, I want the launcher to run natively on my Apple Silicon Mac without Rosetta translation, so that I get optimal performance and battery life.

#### Acceptance Criteria

1. WHEN the launcher executes THEN the system SHALL run using arm64-native Java runtime
2. WHEN the launcher displays the UI THEN the system SHALL render using arm64-native JavaFX libraries
3. WHEN checking the process architecture THEN the system SHALL report as "arm64" not "x86_64"
4. THE launcher SHALL NOT require Rosetta 2 translation layer for execution

### Requirement 3

**User Story:** As a macOS user, I want the launcher application to follow macOS conventions and integrate properly with the operating system, so that it feels like a native Mac application.

#### Acceptance Criteria

1. WHEN the application is installed THEN the system SHALL place it in the /Applications directory
2. WHEN viewing the application in Finder THEN the system SHALL display the Woodlanders icon
3. WHEN right-clicking the application THEN the system SHALL show "Get Info" with proper application metadata including version and bundle identifier
4. WHEN the application launches THEN the system SHALL display in the Dock with the Woodlanders icon
5. THE application bundle SHALL follow standard macOS .app structure with Contents/MacOS, Contents/Resources, and Contents/Info.plist

### Requirement 4

**User Story:** As a macOS user, I want the launcher to store game files and configuration in standard macOS locations, so that my data is organized according to macOS conventions.

#### Acceptance Criteria

1. WHEN the launcher stores game files THEN the system SHALL use ~/Library/Application Support/Woodlanders/ directory
2. WHEN the launcher stores cache files THEN the system SHALL use ~/Library/Caches/woodlanders-javafx/ directory
3. WHEN the launcher stores configuration THEN the system SHALL use ~/Library/Application Support/Woodlanders/ directory
4. THE launcher SHALL create these directories if they do not exist
5. WHEN the user uninstalls the application THEN the system SHALL allow manual cleanup of these standard locations

### Requirement 5

**User Story:** As a developer, I want to build the macOS package using Gradle tasks integrated with the existing build system, so that I can create releases consistently alongside other platform packages.

#### Acceptance Criteria

1. WHEN executing the build task THEN the system SHALL download arm64 JDK 21 for macOS if not cached
2. WHEN executing the build task THEN the system SHALL download arm64 JavaFX 21 for macOS if not cached
3. WHEN executing the build task THEN the system SHALL create the .app bundle structure with proper directory hierarchy
4. WHEN executing the build task THEN the system SHALL copy the launcher JAR and dependencies into the bundle
5. WHEN executing the build task THEN the system SHALL generate Info.plist with correct metadata
6. WHEN executing the build task THEN the system SHALL create a launcher script that invokes Java with proper module path and JavaFX arguments
7. WHEN executing the build task THEN the system SHALL package the .app bundle into a DMG file
8. THE build task SHALL be named 'macosPackage' following the naming convention of existing platform tasks

### Requirement 6

**User Story:** As a macOS user, I want the launcher to handle macOS security features appropriately, so that I can run the application without excessive security warnings.

#### Acceptance Criteria

1. WHEN the DMG is downloaded THEN the system SHALL include a README or instructions about first-run security
2. WHEN the user first opens the application THEN the system SHALL display standard macOS Gatekeeper warning for unsigned applications
3. THE application bundle SHALL include proper Info.plist entries for security permissions
4. WHERE network access is required THEN the Info.plist SHALL declare network client entitlements
5. WHERE file system access is required THEN the Info.plist SHALL declare appropriate file access permissions

### Requirement 7

**User Story:** As a developer, I want the macOS build to integrate with the existing buildAllPackages task, so that I can create all platform packages in a single command.

#### Acceptance Criteria

1. WHEN executing buildAllPackages THEN the system SHALL include the macOS package in the build process
2. WHEN the macOS build completes THEN the system SHALL output the DMG file to app/build/distributions/
3. WHEN buildAllPackages completes THEN the system SHALL display the macOS package path in the summary output
4. WHERE the build is executed on a non-macOS system THEN the system SHALL skip the macOS package with a warning message
5. THE macOS package task SHALL only execute when running on macOS with required tools available

### Requirement 8

**User Story:** As a macOS user, I want clear documentation on how to install and run the launcher, so that I can successfully use the application on my Mac.

#### Acceptance Criteria

1. WHEN the DMG is created THEN the system SHALL include a README file with installation instructions
2. THE README SHALL explain how to handle Gatekeeper warnings for unsigned applications
3. THE README SHALL document the keyboard shortcut (Control+click or right-click → Open) for first launch
4. THE README SHALL list system requirements including macOS version and Apple Silicon requirement
5. THE README SHALL explain where game files and configuration are stored

### Requirement 9

**User Story:** As a user, I want the launcher to function identically on macOS as it does on other platforms, so that I have a consistent experience regardless of operating system.

#### Acceptance Criteria

1. WHEN the launcher checks for updates THEN the system SHALL use the same GitHub API integration as other platforms
2. WHEN the launcher downloads the game client THEN the system SHALL use the same download and verification logic as other platforms
3. WHEN the launcher launches the game THEN the system SHALL execute java -jar with the downloaded client JAR
4. WHEN the launcher displays the UI THEN the system SHALL show the same interface as other platforms
5. THE launcher SHALL maintain version metadata in the same JSON format as other platforms

### Requirement 10

**User Story:** As a developer, I want to optionally code-sign the macOS application, so that users can run it without Gatekeeper warnings when I have a developer certificate.

#### Acceptance Criteria

1. WHERE a developer certificate is available THEN the build system SHALL support code signing the .app bundle
2. WHERE code signing is configured THEN the system SHALL sign all native binaries in the bundle
3. WHERE code signing is configured THEN the system SHALL sign the .app bundle itself
4. WHERE no certificate is configured THEN the system SHALL create an unsigned bundle with a warning message
5. THE build system SHALL document how to configure code signing credentials
