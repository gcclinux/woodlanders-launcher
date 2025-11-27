# Implementation Plan

- [x] 1. Update LauncherPaths to support macOS directory conventions
  - Modify LauncherPaths.java to detect macOS via System.getProperty("os.name")
  - Return ~/Library/Application Support/Woodlanders/ for config directory on macOS
  - Return ~/Library/Caches/woodlanders-javafx/ for JavaFX cache on macOS
  - Ensure directory creation logic works on macOS
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 1.1 Write property test for macOS path resolution
  - **Property 3: macOS path resolution**
  - **Validates: Requirements 4.1, 4.2, 4.3, 4.4**


- [x] 2. Create macOS icon in ICNS format
  - Convert existing PNG icon to ICNS format using iconutil or sips
  - Create icon/woodlanders-launcher.icns file
  - Ensure icon includes multiple resolutions (16x16 to 512x512)
  - _Requirements: 3.2_

- [x] 3. Create Info.plist template
  - Create macos/Info.plist.template with all required keys
  - Include CFBundleIdentifier, CFBundleVersion, CFBundleExecutable, CFBundleIconFile
  - Include LSMinimumSystemVersion (11.0), NSHighResolutionCapable (true)
  - Include security and permission keys for network and file access
  - Use placeholder variables for version and other dynamic values
  - _Requirements: 3.3, 5.5, 6.3, 6.4, 6.5_

- [x] 4. Create native launcher script template
  - Create macos/launcher.sh.template with bash shebang
  - Set JAVA_HOME to bundled JRE location
  - Set JAVAFX_PATH to bundled JavaFX libraries
  - Set javafx.cachedir to ~/Library/Caches/woodlanders-javafx/
  - Construct java command with module-path and add-modules arguments
  - Execute launcher JAR with proper arguments
  - _Requirements: 5.6_

- [x] 5. Create macOS README documentation
  - Create macos/README.md with installation instructions
  - Document how to handle Gatekeeper warnings (Control+click → Open)
  - List system requirements (macOS 11+, Apple Silicon)
  - Explain file storage locations (~/Library/Application Support/Woodlanders/)
  - Include troubleshooting section
  - _Requirements: 6.1, 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 6. Implement Gradle task for downloading and caching JDK
  - Create downloadMacOSJDK task in app/build.gradle
  - Define JDK download URL for arm64 macOS (Adoptium Temurin 21.0.5+11)
  - Define expected SHA256 checksum
  - Check if JDK already cached in ~/.gradle/caches/woodlanders-macos/
  - Download JDK if not cached or checksum mismatch
  - Verify downloaded file checksum
  - _Requirements: 5.1_

- [x] 6.1 Write property test for download caching
  - **Property 6: Download caching behavior**
  - **Validates: Requirements 5.1, 5.2**

- [x] 7. Implement Gradle task for downloading and caching JavaFX
  - Create downloadMacOSJavaFX task in app/build.gradle
  - Define JavaFX SDK download URL for arm64 macOS (Gluon 21.0.4)
  - Define expected SHA256 checksum
  - Check if JavaFX already cached in ~/.gradle/caches/woodlanders-macos/
  - Download JavaFX if not cached or checksum mismatch
  - Verify downloaded file checksum
  - _Requirements: 5.2_

- [x] 8. Implement Gradle task to create .app bundle structure
  - Create createMacOSAppBundle task in app/build.gradle
  - Create WoodlandersLauncher.app/Contents/ directory structure
  - Create Contents/MacOS/, Contents/Resources/, Contents/Resources/app/lib/ directories
  - Set proper directory permissions
  - _Requirements: 5.3_

- [x] 8.1 Write property test for bundle structure
  - **Property 1: Bundle structure completeness**
  - **Validates: Requirements 1.3, 3.5, 5.3, 5.4**

- [x] 9. Implement Gradle task to populate .app bundle with JDK
  - Create extractMacOSJDK task in app/build.gradle
  - Extract downloaded JDK tarball to Contents/Resources/app/jre/
  - Verify Java executable exists at jre/bin/java
  - Set executable permissions on Java binaries
  - _Requirements: 1.3, 1.4_

- [x] 9.1 Write property test for architecture verification
  - **Property 2: Architecture consistency**
  - **Validates: Requirements 1.4, 1.5, 2.1, 2.2, 2.3, 2.4**

- [x] 10. Implement Gradle task to populate .app bundle with JavaFX
  - Create extractMacOSJavaFX task in app/build.gradle
  - Extract JavaFX SDK zip to temporary location
  - Copy JavaFX JAR files to jre/lib/
  - Copy JavaFX native libraries (*.dylib) to jre/lib/
  - Verify JavaFX modules are present
  - _Requirements: 1.3, 1.5_

- [x] 11. Implement Gradle task to copy launcher application files
  - Create copyMacOSLauncherFiles task in app/build.gradle
  - Depend on jar task to ensure launcher JAR is built
  - Copy woodlanders-launcher.jar to Contents/Resources/app/lib/
  - Copy all dependency JARs to Contents/Resources/app/lib/
  - Copy ICNS icon to Contents/Resources/
  - _Requirements: 5.4_

- [x] 12. Implement Gradle task to generate Info.plist
  - Create generateMacOSInfoPlist task in app/build.gradle
  - Read Info.plist.template file
  - Replace version placeholder with actual version (0.1.0)
  - Replace bundle identifier placeholder
  - Write generated Info.plist to Contents/Info.plist
  - _Requirements: 5.5_

- [x] 12.1 Write property test for Info.plist correctness
  - **Property 4: Info.plist correctness**
  - **Validates: Requirements 3.3, 5.5, 6.3**

- [x] 13. Implement Gradle task to create launcher script
  - Create generateMacOSLauncherScript task in app/build.gradle
  - Read launcher.sh.template file
  - Generate script with correct paths to bundled JRE and JavaFX
  - Write script to Contents/MacOS/WoodlandersLauncher
  - Set executable permissions (chmod 755)
  - _Requirements: 5.6_

- [x] 13.1 Write property test for launcher script validity
  - **Property 5: Launcher script validity**
  - **Validates: Requirements 5.6**

- [x] 14. Implement Gradle task to create PkgInfo file
  - Create generateMacOSPkgInfo task in app/build.gradle
  - Write PkgInfo file with "APPLWDLN" (type + signature)
  - Place in Contents/PkgInfo
  - _Requirements: 3.5_

- [x] 15. Implement Gradle task to create DMG
  - Create createMacOSDMG task in app/build.gradle
  - Use hdiutil create command to package .app bundle
  - Set DMG volume name to "Woodlanders Launcher"
  - Use UDZO compression format
  - Output to app/build/distributions/woodlanders-launcher-macos-0.1.0.dmg
  - Copy README.md into DMG alongside .app bundle
  - _Requirements: 1.1, 5.7_

- [x] 15.1 Write integration test for DMG creation
  - Verify DMG file exists at expected location
  - Verify DMG can be mounted with hdiutil
  - Verify .app bundle is present in mounted DMG
  - **Validates: Requirements 1.1, 5.7**

- [x] 16. Implement main macosPackage task
  - Create macosPackage task in app/build.gradle
  - Add dependencies on all previous tasks in correct order
  - Add onlyIf condition to check for macOS operating system
  - Add onlyIf condition to check for required tools (hdiutil, iconutil)
  - Display warning message if skipping due to non-macOS system
  - _Requirements: 5.8, 7.4, 7.5_

- [x] 16.1 Write property test for conditional execution
  - **Property 9: Conditional build execution**
  - **Validates: Requirements 7.4, 7.5**

- [x] 17. Integrate macosPackage into buildAllPackages task
  - Add macosPackage as dependency to buildAllPackages task
  - Update buildAllPackages summary output to include macOS DMG path
  - Ensure summary shows "(skipped)" message when not on macOS
  - _Requirements: 7.1, 7.2, 7.3_

- [x] 17.1 Write property test for build output location
  - **Property 8: Build output location**
  - **Validates: Requirements 7.2**

- [x] 18. Implement optional code signing support
  - Add codesignMacOSApp task in app/build.gradle
  - Check for MACOS_CODESIGN_IDENTITY environment variable or gradle property
  - If configured, sign all binaries in jre/bin/ and jre/lib/
  - Sign the .app bundle with --deep --force flags
  - Verify signatures with codesign --verify
  - If not configured, skip with informational message
  - Make codesignMacOSApp run after bundle creation but before DMG creation
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 18.1 Write property test for code signing
  - **Property 10: Code signing completeness**
  - **Validates: Requirements 10.1, 10.2, 10.3**

- [x] 19. Add macOS build documentation to BUILD_GUIDE.md
  - Document macOS build requirements (macOS 11+, Xcode Command Line Tools)
  - Document how to run: gradle macosPackage
  - Document output location: app/build/distributions/
  - Document optional code signing configuration
  - Document that build only works on macOS systems
  - _Requirements: 10.5_

- [x] 20. Update main README.md with macOS installation instructions
  - Add macOS section to Quick Start
  - Document downloading DMG from releases
  - Document drag-and-drop installation to Applications
  - Document first-run Gatekeeper handling (Control+click → Open)
  - Document system requirements (macOS 11+, Apple Silicon)
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [x] 21. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 22. Test complete build on macOS Apple Silicon
  - Run gradle macosPackage on M-series Mac
  - Verify DMG is created successfully
  - Mount DMG and verify .app bundle structure
  - Copy app to Applications folder
  - Launch app and verify it starts without errors
  - Verify app runs as arm64 (check Activity Monitor)
  - Verify game download and launch functionality
  - Verify files are created in ~/Library/Application Support/Woodlanders/
  - Test Gatekeeper behavior on fresh install
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.3, 3.1, 3.4, 4.1, 4.2, 9.1, 9.2, 9.3_

- [x] 23. Write property test for platform-independent functionality
  - **Property 7: Platform-independent functionality**
  - **Validates: Requirements 9.1, 9.2, 9.3, 9.5**
