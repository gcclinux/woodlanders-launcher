# Woodlanders Launcher - Windows Installation

## System Requirements
- Windows 10 or later
- Java 17 or higher (JRE or JDK)

## Installing Java (if not already installed)
1. Download Java 17+ from [https://adoptium.net/](https://adoptium.net/)
2. Run the installer and follow the instructions
3. Make sure "Add to PATH" is checked during installation

## Running the Launcher

### Method 1: Using the Batch File (Easiest)
1. Extract the ZIP file to a folder of your choice
2. Double-click `launcher.bat`
3. The Woodlanders Launcher will start

### Method 2: Using PowerShell Script
1. Extract the ZIP file
2. Right-click `launcher.ps1` and select "Run with PowerShell"
3. If you get a security warning, you may need to run:
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

### Method 3: Using Command Line
1. Extract the ZIP file
2. Open Command Prompt or PowerShell in the extracted folder
3. Run:
   ```
   java -jar woodlanders-launcher.jar
   ```

## Troubleshooting

### "Java is not recognized as an internal or external command"
- Java is not installed or not in your PATH
- Install Java from [https://adoptium.net/](https://adoptium.net/)
- Make sure to check "Add to PATH" during installation

### Application doesn't start
- Make sure you have Java 17 or higher
- Check Java version: `java -version`
- Try running from command line to see error messages

### Creating a Desktop Shortcut
1. Right-click `launcher.bat`
2. Select "Create shortcut"
3. Drag the shortcut to your desktop
4. (Optional) Right-click the shortcut → Properties → Change Icon → Browse to `launcher.ico`

## Building from Source
See the main README.md for build instructions.
