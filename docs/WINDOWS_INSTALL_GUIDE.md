# Windows Installation Guide

## Easy Installation (Recommended) ⭐

### What You Get:
- **One-click installer** that handles everything
- **Automatic Java download** if you don't have it (no separate installation needed!)
- **Desktop shortcut** for easy access
- **Start Menu integration**
- **Professional installation** like any other Windows app

### Installation Steps:

1. **Download** the installer package:
   - `woodlanders-launcher-windows-installer-0.1.0.zip`

2. **Extract** the ZIP file to any folder (e.g., Downloads)

3. **Run the installer**:
   - Double-click `INSTALL.bat`
   - The installer will:
     ✓ Check if Java is installed
     ✓ Download Java automatically if needed (~50-80 MB)
     ✓ Install the launcher to `%LOCALAPPDATA%\Woodlanders\Launcher`
     ✓ Create desktop and Start Menu shortcuts
     ✓ Optionally launch the app when done

4. **Launch**:
   - Use the desktop shortcut, or
   - Find it in Start Menu > Woodlanders

### What Happens During Installation:

```
Checking for Java...
  ↓
Java found? YES → Use system Java
           NO  → Download bundled Java (automatic)
  ↓
Install launcher files
  ↓
Create shortcuts
  ↓
Done! Launch app
```

### System Requirements:
- Windows 10 or later
- 500 MB free disk space
- Internet connection (only for initial Java download if needed)

### Uninstallation:
To uninstall, delete these folders:
- `%LOCALAPPDATA%\Woodlanders\Launcher`
- Desktop shortcut
- `%APPDATA%\Microsoft\Windows\Start Menu\Programs\Woodlanders`

---

## Alternative: Manual Installation

If you prefer manual control or the automatic installer doesn't work:

1. Download `woodlanders-launcher-windows-0.1.0.zip`
2. Install Java 17+ from https://adoptium.net/
3. Extract the ZIP and double-click `launcher.bat`

---

## Troubleshooting

### "Script execution is disabled"
If the PowerShell installer doesn't run:
1. Open PowerShell as Administrator
2. Run: `Set-ExecutionPolicy RemoteSigned`
3. Try installation again

### "Installation failed"
- Make sure you have internet connection
- Try running `INSTALL.bat` as Administrator
- Check if Windows Defender is blocking the scripts
- Use manual installation method instead

### "Java download failed"
- Check your internet connection
- Try downloading Java manually from https://adoptium.net/
- Use the manual installation ZIP package

### App won't start after installation
- The launcher checks for Java on first run
- If issues persist, try manual installation
- Check the README.md for more details

---

## For Advanced Users

### Building a True MSI/EXE Installer

The current installer is a PowerShell script that auto-downloads Java. To create a true native Windows installer (.msi or .exe):

**Option 1: Use jpackage on Windows**
```bash
# On a Windows machine with JDK 17+
gradle jpackage
```
This creates a professional MSI installer with bundled JRE (~100-150 MB).

**Option 2: Use WiX Toolset**
- Install WiX Toolset on Windows
- Use the jlink plugin configuration already in build.gradle
- Build on Windows machine

**Why not build .msi/.exe from Linux?**
- jpackage requires platform-specific tools (WiX for Windows)
- These tools don't work in WSL/Linux
- Cross-compilation for Windows installers is complex and unreliable

**Current Solution Benefits:**
- ✓ Works from Linux/WSL
- ✓ Auto-downloads Java
- ✓ Professional user experience
- ✓ Creates shortcuts
- ✓ Easy uninstall
- ✗ Not a "true" .exe installer (but users won't notice the difference!)

---

## Comparison of Distribution Methods

| Method | File Size | User Experience | Java Required | Build From Linux |
|--------|-----------|-----------------|---------------|------------------|
| **Installer ZIP** (Recommended) | ~10 MB | ⭐⭐⭐⭐⭐ Auto-installs Java | No | ✓ Yes |
| **Manual ZIP** | ~10 MB | ⭐⭐⭐ Requires Java install | Yes | ✓ Yes |
| **MSI/EXE** (jpackage) | ~100 MB | ⭐⭐⭐⭐⭐ Native installer | No (bundled) | ✗ No (Windows only) |
| **Snap** | ~120 MB | ⭐⭐⭐⭐ (Linux only) | No (bundled) | ✓ Yes |

The **Installer ZIP** provides the best balance of:
- Small download size
- Professional installation experience
- Cross-platform build capability
- Automatic Java handling
