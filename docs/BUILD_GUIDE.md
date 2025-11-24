# Building Distribution Packages

This document explains how to create distribution packages for the Woodlanders Launcher.

## Quick Start

### Build All Packages (Windows + Linux)
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
```

## Output Locations

- **Windows Manual**: `app/build/distributions/woodlanders-launcher-windows-0.1.0.zip`
- **Windows Installer**: `app/build/distributions/woodlanders-launcher-windows-installer-0.1.0.zip` ⭐
- **Linux TAR.GZ**: `app/build/distributions/woodlanders-launcher-linux-0.1.0.tar.gz`
- **Linux Snap**: `app/build/distributions/woodlanders-launcher_0.1.0_amd64.snap`

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

## Cross-Platform Building

### Can You Build Windows Packages on Linux?
**Yes!** The build system creates Windows packages from Linux without any issues.

The packages include:
1. **Shadow JAR**: A "fat JAR" containing all dependencies that runs on any platform with Java 17+
2. **Launch Scripts**: Platform-specific scripts (`.bat` for Windows, `.sh` for Linux)

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

### For Building
- **Java**: JDK 17+
- **Gradle**: 9.2.1+ (wrapper included)
- **Snapcraft**: For building snap packages (Linux only)

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
