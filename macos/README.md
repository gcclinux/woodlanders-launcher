# Woodlanders Launcher for macOS

This is the native macOS application package for the Woodlanders Launcher, designed specifically for Apple Silicon (M1, M2, M3, M4) Macs.

## System Requirements

- **Operating System**: macOS 11.0 (Big Sur) or later
- **Processor**: Apple Silicon (M1, M2, M3, M4 series)
- **Disk Space**: Approximately 300 MB for the application and game files
- **Internet Connection**: Required for downloading game updates

## Installation

1. **Download the DMG**: Download `woodlanders-launcher-macos-{version}.dmg` from the releases page

2. **Open the DMG**: Double-click the downloaded DMG file to mount it

3. **Install the Application**: Drag the `WoodlandersLauncher.app` icon to your Applications folder

4. **Eject the DMG**: After copying, eject the disk image from Finder

## First Launch - Handling Gatekeeper

Since this application is not signed with an Apple Developer certificate, macOS Gatekeeper will prevent it from opening normally on first launch.

### To open the application for the first time:

1. **Navigate to Applications**: Open Finder and go to your Applications folder

2. **Control+Click (or Right-Click)**: Hold the Control key and click on `WoodlandersLauncher.app`, or right-click it

3. **Select "Open"**: Choose "Open" from the context menu

4. **Confirm**: Click "Open" in the security dialog that appears

**Important**: You only need to do this once. After the first launch, you can open the application normally by double-clicking.

### Alternative Method:

If the Control+click method doesn't work:

1. Open **System Settings** (or System Preferences on older macOS versions)
2. Go to **Privacy & Security**
3. Scroll down to find the message about WoodlandersLauncher being blocked
4. Click **Open Anyway**

## File Storage Locations

The Woodlanders Launcher follows macOS conventions for file storage:

### Game Files and Configuration
```
~/Library/Application Support/Woodlanders/
```
This directory contains:
- Downloaded game client JAR files
- Version metadata (version.json)
- Configuration files

### Cache Files
```
~/Library/Caches/woodlanders-javafx/
```
This directory contains:
- JavaFX runtime cache files
- Temporary files

### Accessing Library Folder

The `~/Library` folder is hidden by default in macOS. To access it:

1. Open Finder
2. Click the **Go** menu while holding the **Option** key
3. Select **Library** from the menu
4. Navigate to `Application Support/Woodlanders/`

## Using the Launcher

1. **Launch the Application**: Open WoodlandersLauncher from your Applications folder

2. **Check for Updates**: The launcher will automatically check for the latest Woodlanders game version

3. **Download**: If an update is available, click the download button to get the latest version

4. **Play**: Once downloaded, click the "Launch Game" button to start playing

## Troubleshooting

### Application Won't Open

**Problem**: Double-clicking the app does nothing or shows a security warning

**Solution**: Follow the "First Launch - Handling Gatekeeper" instructions above

---

**Problem**: Error message about damaged or incomplete application

**Solution**: 
1. Delete the application from Applications folder
2. Re-download the DMG file (previous download may be corrupted)
3. Reinstall following the installation steps

### Application Crashes on Launch

**Problem**: Application opens briefly then closes

**Solution**:
1. Check that you're running macOS 11.0 or later
2. Verify you have an Apple Silicon Mac (not Intel)
3. Check Console.app for error messages (search for "WoodlandersLauncher")
4. Try removing the cache directory: `~/Library/Caches/woodlanders-javafx/`

### Game Won't Download

**Problem**: Download fails or gets stuck

**Solution**:
1. Check your internet connection
2. Verify you have sufficient disk space (at least 200 MB free)
3. Check that `~/Library/Application Support/Woodlanders/` is writable
4. Try removing old game files from the directory and downloading again

### Game Won't Launch

**Problem**: Download succeeds but game doesn't start

**Solution**:
1. Verify the downloaded JAR file exists in `~/Library/Application Support/Woodlanders/`
2. Check that the file isn't corrupted (re-download if necessary)
3. Look for error messages in the launcher window
4. Check Console.app for Java-related errors

### Performance Issues

**Problem**: Application runs slowly or uses excessive resources

**Solution**:
1. Check Activity Monitor to verify the process is running as "arm64" (not "Intel" under Rosetta)
2. If running under Rosetta, you may have downloaded the wrong version - ensure you have the Apple Silicon version
3. Close other applications to free up system resources
4. Restart your Mac

### Permission Errors

**Problem**: Cannot create or access game files

**Solution**:
1. Ensure the launcher has permission to access your files
2. Check that `~/Library/Application Support/` is writable
3. Try manually creating the directory: `mkdir -p ~/Library/Application\ Support/Woodlanders/`
4. Check disk permissions with Disk Utility

## Uninstalling

To completely remove the Woodlanders Launcher:

1. **Delete the Application**:
   - Move `WoodlandersLauncher.app` from Applications to Trash

2. **Remove Game Files** (optional):
   ```bash
   rm -rf ~/Library/Application\ Support/Woodlanders/
   ```

3. **Remove Cache Files** (optional):
   ```bash
   rm -rf ~/Library/Caches/woodlanders-javafx/
   ```

## Technical Details

### Bundled Components

This application includes:
- **Java Runtime Environment (JRE) 21**: arm64 native build from Adoptium Temurin
- **JavaFX 21**: arm64 native libraries from Gluon
- **Woodlanders Launcher**: The launcher application and its dependencies

All components are bundled within the application, so you don't need to install Java separately.

### Architecture

The application runs natively on Apple Silicon (arm64) without requiring Rosetta 2 translation. This provides optimal performance and battery efficiency.

### Privacy

The launcher:
- Connects to GitHub API to check for game updates
- Downloads game files from GitHub releases
- Stores files only in the documented locations
- Does not collect or transmit any personal information

## Getting Help

If you encounter issues not covered in this troubleshooting guide:

1. Check the project's GitHub Issues page for known problems
2. Review the Console.app logs for detailed error messages
3. Create a new issue on GitHub with:
   - Your macOS version
   - Your Mac model (M1, M2, M3, or M4)
   - Steps to reproduce the problem
   - Any error messages from Console.app

## Building from Source

If you want to build the macOS package yourself, see the main project BUILD_GUIDE.md for instructions on using the `gradle macosPackage` task.

**Note**: Building the macOS package requires:
- macOS 11.0 or later
- Xcode Command Line Tools
- Gradle 7.0 or later
