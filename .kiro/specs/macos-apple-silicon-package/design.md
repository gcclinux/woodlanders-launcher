# Design Document: macOS Apple Silicon Package

## Overview

This design specifies the implementation of a native macOS application package for the Woodlanders Launcher targeting Apple Silicon (arm64) processors. The solution will create a standard macOS .app bundle containing the launcher application, bundled JDK 21 (arm64), and JavaFX 21 (arm64), packaged in a DMG disk image for distribution. The implementation will integrate with the existing Gradle build system, following patterns established by the Windows, Snap, and Flatpak packages.

The macOS package will provide identical functionality to other platforms: checking GitHub for game updates, downloading the Woodlanders client, and launching the game. The key difference is the packaging format and the use of macOS-specific conventions for file locations and application structure.

## Architecture

### Build System Architecture

The macOS package build will be implemented as a new Gradle task (`macosPackage`) that:
1. Downloads and caches arm64 JDK 21 and JavaFX 21 binaries
2. Creates the .app bundle directory structure
3. Copies the launcher JAR and dependencies
4. Generates the Info.plist metadata file
5. Creates a native launcher script
6. Packages the .app bundle into a DMG file

The build will only execute on macOS systems (detected via `System.getProperty('os.name')`), similar to how the snap and flatpak tasks conditionally execute.

### Application Bundle Structure

```
WoodlandersLauncher.app/
├── Contents/
│   ├── Info.plist                    # Application metadata
│   ├── MacOS/
│   │   └── WoodlandersLauncher       # Native launcher script
│   ├── Resources/
│   │   ├── woodlanders-launcher.icns # Application icon
│   │   └── app/                      # Launcher application
│   │       ├── lib/                  # JAR dependencies
│   │       │   ├── woodlanders-launcher.jar
│   │       │   ├── jackson-*.jar
│   │       │   └── ...
│   │       └── jre/                  # Bundled Java runtime
│   │           ├── bin/
│   │           │   └── java
│   │           ├── lib/
│   │           │   ├── *.dylib
│   │           │   ├── javafx-*.jar  # JavaFX modules
│   │           │   └── ...
│   │           └── ...
│   └── PkgInfo                       # Legacy type/creator codes
```

### Runtime Architecture

When launched, the native launcher script will:
1. Set `JAVA_HOME` to the bundled JRE
2. Construct the JavaFX module path from bundled libraries
3. Execute: `java --module-path <javafx-path> --add-modules javafx.controls -jar woodlanders-launcher.jar`
4. Set JavaFX cache directory to `~/Library/Caches/woodlanders-javafx/`

## Components and Interfaces

### 1. Gradle Build Task: `macosPackage`

**Responsibility**: Orchestrate the complete macOS package build process

**Key Methods**:
- `downloadJDK()`: Download arm64 JDK 21 from Adoptium if not cached
- `downloadJavaFX()`: Download arm64 JavaFX 21 from Gluon if not cached
- `createAppBundle()`: Create .app directory structure
- `copyLauncherFiles()`: Copy JAR and dependencies
- `extractJDK()`: Extract JDK into bundle
- `extractJavaFX()`: Extract JavaFX libraries into JDK
- `generateInfoPlist()`: Create Info.plist from template
- `createLauncherScript()`: Generate native launcher script
- `createDMG()`: Package .app into DMG using hdiutil

**Dependencies**:
- Existing `jar` task output
- macOS `hdiutil` command-line tool
- Internet connection for downloading JDK/JavaFX (first build only)

### 2. Info.plist Generator

**Responsibility**: Generate macOS application metadata

**Key Properties**:
```xml
CFBundleName: Woodlanders Launcher
CFBundleDisplayName: Woodlanders Launcher
CFBundleIdentifier: io.github.gcclinux.woodlanders.launcher
CFBundleVersion: 0.1.0
CFBundleShortVersionString: 0.1.0
CFBundlePackageType: APPL
CFBundleSignature: WDLN
CFBundleExecutable: WoodlandersLauncher
CFBundleIconFile: woodlanders-launcher.icns
LSMinimumSystemVersion: 11.0
NSHighResolutionCapable: true
NSSupportsAutomaticGraphicsSwitching: true
```

### 3. Native Launcher Script

**Responsibility**: Bootstrap the Java application with correct environment

**Script Template**:
```bash
#!/bin/bash
DIR="$(cd "$(dirname "$0")/.." && pwd)"
JAVA_HOME="$DIR/Resources/app/jre"
JAVAFX_PATH="$JAVA_HOME/lib"
CACHE_DIR="$HOME/Library/Caches/woodlanders-javafx"

export JAVA_HOME
mkdir -p "$CACHE_DIR"

exec "$JAVA_HOME/bin/java" \
  --module-path "$JAVAFX_PATH" \
  --add-modules javafx.controls \
  -Djavafx.cachedir="$CACHE_DIR" \
  -jar "$DIR/Resources/app/lib/woodlanders-launcher.jar"
```

### 4. DMG Creator

**Responsibility**: Package .app bundle into distributable disk image

**Implementation**: Use macOS `hdiutil` command:
```bash
hdiutil create -volname "Woodlanders Launcher" \
  -srcfolder WoodlandersLauncher.app \
  -ov -format UDZO \
  woodlanders-launcher-macos-0.1.0.dmg
```

### 5. Icon Converter

**Responsibility**: Convert PNG icon to ICNS format

**Implementation**: Use macOS `iconutil` or `sips` command to convert existing PNG icon to ICNS format required by macOS.

### 6. Path Configuration Adapter

**Responsibility**: Adapt file paths for macOS conventions

**Changes Required in LauncherPaths.java**:
- Detect macOS via `System.getProperty("os.name")`
- Use `~/Library/Application Support/Woodlanders/` instead of `~/.config/woodlanders/`
- Use `~/Library/Caches/woodlanders-javafx/` for JavaFX cache

## Data Models

### Build Configuration

```groovy
class MacOSPackageConfig {
    String jdkVersion = "21.0.5+11"
    String jdkDownloadUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_aarch64_mac_hotspot_21.0.5_11.tar.gz"
    String jdkSha256 = "..." // Checksum for verification
    
    String javafxVersion = "21.0.4"
    String javafxDownloadUrl = "https://download2.gluonhq.com/openjfx/21.0.4/openjfx-21.0.4_macos-aarch64_bin-sdk.zip"
    String javafxSha256 = "..." // Checksum for verification
    
    String appVersion = "0.1.0"
    String bundleIdentifier = "io.github.gcclinux.woodlanders.launcher"
    String appName = "Woodlanders Launcher"
}
```

### Info.plist Template

The Info.plist will be generated from a template with variable substitution for version, bundle identifier, and other metadata.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Bundle structure completeness
*For any* successfully built .app bundle, the bundle SHALL contain all required components in the correct locations: Contents/Info.plist, Contents/MacOS/launcher script, Contents/Resources/app/lib/*.jar files, Contents/Resources/app/jre/ with Java binaries, and JavaFX libraries
**Validates: Requirements 1.3, 3.5, 5.3, 5.4**

### Property 2: Architecture consistency
*For any* binary file in the .app bundle (Java executable in jre/bin/java, native libraries *.dylib), inspecting with the `file` command SHALL report "arm64" architecture
**Validates: Requirements 1.4, 1.5, 2.1, 2.2, 2.3, 2.4**

### Property 3: macOS path resolution
*For any* file operation in the launcher on macOS, the system SHALL use ~/Library/Application Support/Woodlanders/ for game files and configuration, and ~/Library/Caches/woodlanders-javafx/ for JavaFX cache, creating directories if they don't exist
**Validates: Requirements 4.1, 4.2, 4.3, 4.4**

### Property 4: Info.plist correctness
*For any* generated Info.plist file, it SHALL be valid XML (passes `plutil -lint`), contain all required keys (CFBundleIdentifier, CFBundleVersion, CFBundleExecutable, CFBundleIconFile, LSMinimumSystemVersion), and have correct values matching the build configuration
**Validates: Requirements 3.3, 5.5, 6.3**

### Property 5: Launcher script validity
*For any* created launcher script in Contents/MacOS/, the file SHALL be executable (mode 755), contain a valid shebang (#!/bin/bash), set JAVA_HOME to the bundled JRE, and invoke java with correct module-path and JavaFX arguments
**Validates: Requirements 5.6**

### Property 6: Download caching behavior
*For any* build execution, if JDK or JavaFX archives already exist in the cache directory with correct checksums, the system SHALL skip downloading and reuse cached files; if missing or checksum mismatch, SHALL download fresh copies
**Validates: Requirements 5.1, 5.2**

### Property 7: Platform-independent functionality
*For any* launcher operation (GitHub API calls, version comparison, file download, JAR execution), the code path SHALL be identical to other platforms, using the same service classes without macOS-specific branches
**Validates: Requirements 9.1, 9.2, 9.3, 9.5**

### Property 8: Build output location
*For any* successful macOS package build, the DMG file SHALL be created in app/build/distributions/ with the naming pattern woodlanders-launcher-macos-{version}.dmg
**Validates: Requirements 7.2**

### Property 9: Conditional build execution
*For any* execution of macosPackage task, if the OS is not macOS (System.getProperty("os.name") does not contain "Mac"), the task SHALL skip with a warning message and return success without failing the build
**Validates: Requirements 7.4, 7.5**

### Property 10: Code signing completeness
*For any* build with code signing configured (certificate identity provided), the system SHALL sign all native binaries in jre/bin/ and jre/lib/, then sign the .app bundle itself, and all signatures SHALL be verifiable with `codesign --verify`
**Validates: Requirements 10.1, 10.2, 10.3**

## Error Handling

### Build-Time Errors

1. **Missing macOS Tools**: If `hdiutil` or `iconutil` are not available, fail with clear message
2. **Download Failures**: If JDK/JavaFX downloads fail, retry once, then fail with URL and error
3. **Checksum Mismatch**: If downloaded files don't match expected SHA256, delete and fail
4. **Insufficient Disk Space**: Check available space before extracting large archives
5. **Permission Errors**: If unable to set executable permissions, fail with clear message

### Runtime Errors

1. **Missing JRE**: Should not occur (bundled), but launcher script should check and display error
2. **JavaFX Initialization Failure**: Log to console and display error dialog
3. **Path Creation Failure**: If unable to create ~/Library/Application Support/Woodlanders/, display error with permissions guidance
4. **Network Errors**: Handle identically to other platforms (graceful degradation, offline mode)

## Testing Strategy

### Unit Tests

1. **Path Resolution Test**: Verify LauncherPaths returns correct macOS paths when os.name is "Mac OS X"
2. **Info.plist Generation Test**: Verify generated plist contains all required keys with correct values
3. **Version Comparison Test**: Verify version metadata handling works identically across platforms

### Property-Based Tests

Property-based tests will use JUnit 5 with jqwik (Java property-based testing library) to verify universal properties across random inputs.

**Configuration**: Each property test will run a minimum of 100 iterations.

**Tagging**: Each property-based test will include a comment with the format:
`// Feature: macos-apple-silicon-package, Property N: <property text>`

### Integration Tests

1. **Build Task Test**: Execute macosPackage task and verify DMG is created (macOS only)
2. **Bundle Structure Test**: Extract .app bundle and verify all expected files exist
3. **Launcher Execution Test**: Launch the app and verify it starts without errors (manual)
4. **Architecture Verification Test**: Use `file` command to verify all binaries are arm64

### Manual Testing Checklist

1. Build DMG on macOS with Apple Silicon
2. Mount DMG and drag app to Applications
3. Launch app and verify UI appears
4. Verify game download and launch functionality
5. Check Activity Monitor to confirm arm64 process (not x86_64 under Rosetta)
6. Verify files are created in ~/Library/Application Support/Woodlanders/
7. Test on macOS 11, 12, 13, and 14 if possible

## Implementation Notes

### JDK and JavaFX Sources

- **JDK 21 arm64**: Adoptium Temurin builds provide official arm64 macOS binaries
- **JavaFX 21 arm64**: Gluon provides arm64 macOS SDK builds
- Both should be downloaded and cached in `~/.gradle/caches/woodlanders-macos/` to avoid repeated downloads

### Icon Conversion

The existing PNG icon needs conversion to ICNS format. Options:
1. Use `iconutil` with iconset directory (requires multiple sizes)
2. Use `sips` for simple conversion (may not produce optimal results)
3. Pre-generate ICNS file and commit to repository (recommended)

### Code Signing (Optional)

For developers with Apple Developer accounts:
1. Use `codesign` command to sign the .app bundle
2. Sign all native binaries first, then the bundle
3. Command: `codesign --deep --force --verify --verbose --sign "Developer ID Application: <name>" WoodlandersLauncher.app`
4. For distribution outside App Store, need Developer ID certificate
5. May also need notarization for macOS 10.15+

### DMG Customization (Future Enhancement)

Basic DMG will contain just the .app bundle. Future enhancements could include:
- Custom background image
- Positioned icons (app + Applications folder shortcut)
- Custom window size and position
- README file visible in DMG

This requires more complex `hdiutil` usage or tools like `create-dmg` script.

## Dependencies

### Build Dependencies

- macOS 11.0 or later (for building)
- Xcode Command Line Tools (provides `hdiutil`, `iconutil`, `codesign`)
- Gradle 7.0+ (existing)
- Internet connection (first build only, for downloading JDK/JavaFX)

### Runtime Dependencies

- macOS 11.0 or later (Big Sur)
- Apple Silicon (M1, M2, M3, M4) processor
- No additional dependencies (JDK and JavaFX bundled)

### External Resources

- Adoptium Temurin JDK 21 arm64 macOS: ~180 MB download
- Gluon JavaFX 21 arm64 macOS SDK: ~40 MB download
- Total bundle size: ~250 MB (compressed DMG: ~100 MB)

## Platform-Specific Considerations

### macOS Security

1. **Gatekeeper**: Unsigned apps require Control+click → Open on first launch
2. **Quarantine Attribute**: Downloaded DMG will have quarantine flag, requiring user confirmation
3. **Translocation**: macOS may translocate the app to a random location if run from DMG; users should copy to Applications
4. **Privacy Permissions**: Network access is automatic; file access to user directories is allowed

### File System Differences

1. **Case Sensitivity**: macOS file system is case-insensitive by default (unlike Linux)
2. **Path Separators**: Use `/` (same as Linux, different from Windows)
3. **Home Directory**: Use `System.getProperty("user.home")` (works cross-platform)
4. **Standard Directories**: Use `~/Library/Application Support/` not `~/.config/`

### Java Considerations

1. **JavaFX Integration**: Must use `--module-path` and `--add-modules` (same as other platforms)
2. **Retina Display**: Set `NSHighResolutionCapable` to true in Info.plist for proper scaling
3. **Dock Icon**: JavaFX automatically uses icon from Info.plist
4. **Menu Bar**: JavaFX apps get native macOS menu bar automatically

## Migration Path

For users who might have previously run the launcher manually with their own Java installation:

1. The new macOS package uses standard macOS paths, so existing game files in `~/.config/woodlanders/` won't be automatically found
2. Consider adding migration logic to check old location and copy to new location on first launch
3. Document the new file locations in README

## Future Enhancements

1. **Universal Binary**: Support both arm64 and x86_64 in single bundle (for Intel Macs)
2. **App Store Distribution**: Package for Mac App Store (requires sandboxing changes)
3. **Automatic Updates**: Implement Sparkle framework for in-app updates
4. **Notarization**: Automate notarization process for signed builds
5. **Custom DMG**: Enhanced DMG with background image and layout
6. **Homebrew Cask**: Create Homebrew cask formula for easy installation
