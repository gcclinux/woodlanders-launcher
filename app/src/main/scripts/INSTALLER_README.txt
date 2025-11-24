========================================
  Woodlanders Launcher - Easy Installer
  Version: ${VERSION}
========================================

INSTALLATION INSTRUCTIONS
========================================

Quick Start (Recommended):
--------------------------
1. Extract this ZIP file to any folder
2. Double-click "INSTALL.bat"
3. Follow the on-screen instructions
4. The installer will:
   - Download Java automatically if needed (~50-80 MB)
   - Install the launcher to your user directory
   - Create desktop and Start Menu shortcuts
   - Launch the application when done

Alternative Method:
------------------
If INSTALL.bat doesn't work:
1. Right-click "windows-installer.ps1"
2. Select "Run with PowerShell"
3. If you get a security warning, you may need to allow script execution

Manual Installation (Advanced):
-------------------------------
If the automatic installer doesn't work:
1. Install Java 17 or higher from https://adoptium.net/
2. Double-click "launcher.bat" (if available) or run:
   java -jar woodlanders-launcher.jar

What Gets Installed:
-------------------
• Installation Location: %LOCALAPPDATA%\Woodlanders\Launcher
• Java Runtime (if not found): Downloaded automatically from Adoptium
• Desktop Shortcut: Created automatically
• Start Menu Entry: Programs > Woodlanders

Uninstallation:
--------------
To uninstall:
1. Delete the installation folder:
   %LOCALAPPDATA%\Woodlanders\Launcher
2. Delete the desktop shortcut
3. Delete Start Menu folder:
   %APPDATA%\Microsoft\Windows\Start Menu\Programs\Woodlanders

System Requirements:
-------------------
• Windows 10 or later
• 500 MB free disk space
• Internet connection (for initial Java download if needed)

Troubleshooting:
---------------
• If INSTALL.bat doesn't run:
  - Try running as Administrator
  - Try the PowerShell script instead
  
• If you get "cannot be loaded because running scripts is disabled":
  - Open PowerShell as Administrator
  - Run: Set-ExecutionPolicy RemoteSigned
  - Try installation again

• For other issues:
  - Make sure you have internet connection
  - Check Windows Defender isn't blocking the scripts
  - Try manual installation with Java from adoptium.net

Support:
--------
For help and support, visit:
https://github.com/[your-repo]/woodlanders-launcher
