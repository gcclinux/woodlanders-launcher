# Building Distribution Packages

This document explains how to create distribution packages for the Woodlanders Launcher.

## Quick Start

### Build All Packages (Windows + Linux + macOS)
```bash
gradle :app:buildAllPackages
```

### Build Individual Packages
```bash
# Windows ZIP package (manual installation)
gradle :app:windowsPackage

# Windows Installer package (auto-downloads Java)
gradle :app:windowsInstallerPackage

# Linux TAR.GZ package  
gradle :app:linuxPackage

# Linux Snap package (outputs to app/build/distributions/)
snapcraft pack --output=app/build/distributions/woodlanders-launcher_$(grep '^version:' snapcraft.yaml | awk '{print $2}' | tr -d "'")_amd64.snap --destructive-mode

# macOS DMG package (macOS only, requires macOS 11+ and Xcode Command Line Tools)
gradle :app:macosPackage
```

## Output Locations

- **Windows Manual**: `app/build/distributions/woodlanders-launcher-windows-0.1.0.zip`
- **Windows Installer**: `app/build/distributions/woodlanders-launcher-windows-installer-0.1.0.zip` ⭐
- **Linux TAR.GZ**: `app/build/distributions/woodlanders-launcher-linux-0.1.0.tar.gz`
- **Linux Snap**: `app/build/distributions/woodlanders-launcher_0.1.0_amd64.snap`
- **macOS DMG**: `app/build/distributions/woodlanders-launcher-macos-0.1.0.dmg`

**Note**: All packages are now built to the same `app/build/distributions/` directory for consistency.

## What's Included

### Windows Installer Package (`woodlanders-launcher-windows-installer-0.1.0.zip`) ⭐ RECOMMENDED
- `woodlanders-launcher.jar` - Main application (fat JAR with all dependencies)
- `INSTALL.bat` - One-click installer script
- `windows-installer.ps1` - PowerShell installer (auto-downloads Java if needed)
- `INSTALLER_README.txt` - Installation instructions
- `README.md` - Project documentation
- **User Experience**: Extract ZIP → Run INSTALL.bat → Done!
- **Java Handling**: Automatically downloads and installs Java 21 if not found

### Windows Manual Package (`woodlanders-launcher-windows-0.1.0.zip`)
- `woodlanders-launcher.jar` - Main application (fat JAR with all dependencies)
- `launcher.bat` - Windows batch script for easy launching
- `launcher.ps1` - PowerShell script alternative
- `README.md` - Main project documentation
- `WINDOWS_README.md` - Windows-specific instructions
- `*.ico` - Icon files (if available in `/icon` directory)
- **User Experience**: Requires Java 17+ pre-installed

### Linux Package (`woodlanders-launcher-linux-0.1.0.tar.gz`)
- `woodlanders-launcher.jar` - Main application (fat JAR with all dependencies)
- `launcher.sh` - Shell script for launching (executable)
- `README.md` - Project documentation

### Snap Package (`woodlanders-launcher_0.1.0_amd64.snap`)
- Self-contained package with all dependencies including Java runtime
- Installable on any Linux distribution that supports snaps
- Includes desktop integration

### macOS Package (`woodlanders-launcher-macos-0.1.0.dmg`)
- Native macOS .app bundle packaged in a DMG disk image
- Includes bundled JDK 21 (arm64) and JavaFX 21 (arm64)
- Optimized for Apple Silicon (M1, M2, M3, M4) processors
- No Java installation required
- Follows macOS conventions for file storage (~/Library/Application Support/Woodlanders/)
- **Build Requirements**: macOS 11+ with Xcode Command Line Tools
- **User Experience**: Download DMG → Mount → Drag to Applications → Launch

## Cross-Platform Building

### Can You Build Windows Packages on Linux?
**Yes!** The build system creates Windows packages from Linux without any issues.

The packages include:
1. **Shadow JAR**: A "fat JAR" containing all dependencies that runs on any platform with Java 17+
2. **Launch Scripts**: Platform-specific scripts (`.bat` for Windows, `.sh` for Linux)

### Can You Build macOS Packages on Other Platforms?
**No.** The macOS package build requires macOS-specific tools and can only be built on macOS systems:
- Requires `hdiutil` for creating DMG files
- Requires `iconutil` or `sips` for icon conversion
- Requires macOS 11+ with Xcode Command Line Tools installed
- The build task will automatically skip with a warning message when run on non-macOS systems

### Why Not .exe Files?
Creating native Windows `.exe` files from Linux is possible but complex:
- Requires tools like Launch4j, jpackage, or exe4j
- May have compatibility issues across systems
- The JAR + batch script approach is simpler and more reliable

The provided batch scripts (`.bat` and `.ps1`) work just like `.exe` files when double-clicked on Windows.

## System Requirements

### For Users
- **Windows**: Java 17+ JRE
- **Linux**: Java 17+ JRE
- **Snap**: No Java required (bundled)
- **macOS**: No Java required (bundled), macOS 11+ (Big Sur), Apple Silicon processor

### For Building
- **Java**: JDK 17+
- **Gradle**: 9.2.1+ (wrapper included)
- **Snapcraft**: For building snap packages (Linux only)
- **macOS Build Tools**: macOS 11+, Xcode Command Line Tools (for macOS package only)

## Building macOS Packages

### Prerequisites

The macOS package build **only works on macOS systems** and requires:

1. **macOS 11.0 (Big Sur) or later**
2. **Xcode Command Line Tools** - Install with:
   ```bash
   xcode-select --install
   ```
3. **Java JDK 17+** - For building the launcher application
4. **Internet connection** - First build downloads JDK 21 and JavaFX 21 for arm64 (cached for subsequent builds)

### Building the macOS Package

```bash
gradle :app:macosPackage
```

This will:
1. Download and cache arm64 JDK 21 (Adoptium Temurin) if not already cached
2. Download and cache arm64 JavaFX 21 (Gluon) if not already cached
3. Create a WoodlandersLauncher.app bundle with bundled Java runtime
4. Package the .app bundle into a DMG disk image
5. Output to `app/build/distributions/woodlanders-launcher-macos-0.1.0.dmg`

**Note**: The first build will take longer due to downloading ~220 MB of dependencies. Subsequent builds reuse the cached files.

### Output Location

The DMG file will be created at:
```
app/build/distributions/woodlanders-launcher-macos-0.1.0.dmg
```

### Optional: Code Signing

If you have an Apple Developer certificate, you can optionally sign the application:

1. **Set your signing identity** via environment variable:
   ```bash
   export MACOS_CODESIGN_IDENTITY="Developer ID Application: Your Name (TEAM_ID)"
   ```

2. **Or via Gradle property** in `~/.gradle/gradle.properties`:
   ```properties
   macosCodesignIdentity=Developer ID Application: Your Name (TEAM_ID)
   ```

3. **Build with signing**:
   ```bash
   gradle :app:macosPackage
   ```

The build will automatically sign all binaries and the .app bundle if a signing identity is configured. If not configured, the build creates an unsigned bundle (users will need to Control+click → Open on first launch).

### Verifying the Build

After building, you can verify the package:

```bash
# Mount the DMG
hdiutil attach app/build/distributions/woodlanders-launcher-macos-0.1.0.dmg

# Check the app bundle structure
ls -la /Volumes/Woodlanders\ Launcher/WoodlandersLauncher.app/Contents/

# Verify architecture (should show "arm64")
file /Volumes/Woodlanders\ Launcher/WoodlandersLauncher.app/Contents/Resources/app/jre/bin/java

# Unmount
hdiutil detach /Volumes/Woodlanders\ Launcher/
```

### Troubleshooting

**Build fails with "hdiutil: command not found"**
- Install Xcode Command Line Tools: `xcode-select --install`

**Build fails with "iconutil: command not found"**
- Install Xcode Command Line Tools: `xcode-select --install`

**Build skipped with warning on non-macOS system**
- This is expected behavior. The macOS package can only be built on macOS systems.

**Download failures**
- Check your internet connection
- The build will retry once automatically
- Cached files are stored in `~/.gradle/caches/woodlanders-macos/`

## Advanced: Creating Native Executables

If you need true native `.exe` files, you can:

1. **Use jpackage on Windows** (requires building on Windows):
   ```bash
   jpackage --input app/build/libs \
            --name WoodlandersLauncher \
            --main-jar woodlanders-launcher-0.1.0.jar \
            --main-class com.woodlanders.launcher.ui.LauncherApplication \
            --type exe \
            --icon icon/launcher.ico
   ```

2. **Use GraalVM Native Image** (creates standalone binaries):
   - More complex setup
   - Larger initial compile time
   - Smaller runtime footprint
   - No JRE required

For most use cases, the current JAR + script approach is recommended as it's:
- Simple to build and distribute
- Works cross-platform
- Easy to update
- Familiar to Java users

## Distribution Workflow

1. **Development**: Test with `gradle run`
2. **Build**: Run `gradle :app:buildAllPackages`
3. **Test packages**: Extract and test on target platforms
4. **Release**: Upload ZIP/TAR.GZ to GitHub Releases or distribute as needed

## Updating Versions

Update the version in:
- `snapcraft.yaml` (for snap version)
- `app/build.gradle` (archiveVersion in shadowJar, windowsPackage, linuxPackage tasks)
- Scripts will automatically use the updated version via replacement
