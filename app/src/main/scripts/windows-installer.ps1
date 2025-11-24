# Woodlanders Launcher - Windows Installer with JRE Auto-Download
# Version: ${VERSION}

param(
    [switch]$Silent = $false
)

$ErrorActionPreference = "Stop"

# Configuration
$APP_NAME = "Woodlanders Launcher"
$APP_VERSION = "0.1.0"
$JRE_VERSION = "21"
$INSTALL_DIR = "$env:LOCALAPPDATA\Woodlanders\Launcher"
$JRE_DIR = "$INSTALL_DIR\jre"
$DESKTOP_SHORTCUT = "$env:USERPROFILE\Desktop\Woodlanders Launcher.lnk"
$START_MENU_DIR = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Woodlanders"

# Adoptium (Eclipse Temurin) JRE download URLs
$JRE_DOWNLOAD_URL = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse"

function Write-Status {
    param([string]$Message)
    if (-not $Silent) {
        Write-Host $Message -ForegroundColor Cyan
    }
}

function Write-Success {
    param([string]$Message)
    if (-not $Silent) {
        Write-Host "✓ $Message" -ForegroundColor Green
    }
}

function Write-Error-Message {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Test-JavaInstalled {
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
        if ($javaVersion -match "(\d+)\.(\d+)") {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -ge 17) {
                return $true
            }
        }
    } catch {
        return $false
    }
    return $false
}

function Download-JRE {
    Write-Status "Java 21+ not found. Downloading bundled JRE..."
    
    # Create JRE directory
    New-Item -ItemType Directory -Force -Path $JRE_DIR | Out-Null
    
    $jreZip = "$env:TEMP\woodlanders-jre.zip"
    
    try {
        Write-Status "Downloading JRE $JRE_VERSION (this may take a few minutes)..."
        
        # Download JRE
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $JRE_DOWNLOAD_URL -OutFile $jreZip -UseBasicParsing
        $ProgressPreference = 'Continue'
        
        Write-Status "Extracting JRE..."
        Expand-Archive -Path $jreZip -DestinationPath "$JRE_DIR\temp" -Force
        
        # Find the extracted JRE directory (usually has a version number)
        $extractedDir = Get-ChildItem -Path "$JRE_DIR\temp" -Directory | Select-Object -First 1
        
        # Move contents to JRE_DIR
        Get-ChildItem -Path $extractedDir.FullName | Move-Item -Destination $JRE_DIR -Force
        
        # Cleanup
        Remove-Item "$JRE_DIR\temp" -Recurse -Force
        Remove-Item $jreZip -Force
        
        Write-Success "JRE downloaded and installed"
        return $true
    } catch {
        Write-Error-Message "Failed to download JRE: $_"
        return $false
    }
}

function Install-Application {
    Write-Status "Installing $APP_NAME..."
    
    # Create installation directory
    New-Item -ItemType Directory -Force -Path $INSTALL_DIR | Out-Null
    
    # Copy application files
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    
    if (Test-Path "$scriptDir\woodlanders-launcher.jar") {
        Copy-Item "$scriptDir\woodlanders-launcher.jar" -Destination $INSTALL_DIR -Force
        Write-Success "Application files copied"
    } else {
        Write-Error-Message "Application JAR not found!"
        return $false
    }
    
    # Copy icon if exists
    if (Test-Path "$scriptDir\launcher.ico") {
        Copy-Item "$scriptDir\launcher.ico" -Destination $INSTALL_DIR -Force
    }
    
    return $true
}

function Create-LauncherScript {
    $launcherScript = @"
@echo off
setlocal

set "APP_DIR=%~dp0"
set "JAVAFX_CACHE=%USERPROFILE%\.cache\woodlanders-javafx"

if exist "%APP_DIR%jre\bin\java.exe" (
    set "JAVA_CMD=%APP_DIR%jre\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

"%JAVA_CMD%" -Djavafx.cachedir="%JAVAFX_CACHE%" -jar "%APP_DIR%woodlanders-launcher.jar"

if %ERRORLEVEL% neq 0 (
    echo Application failed to start.
    pause
)

endlocal
"@
    
    $launcherScript | Out-File -FilePath "$INSTALL_DIR\launcher.bat" -Encoding ASCII -Force
    Write-Success "Launcher script created"
}

function Create-Shortcuts {
    $WshShell = New-Object -ComObject WScript.Shell
    
    # Desktop shortcut
    $Shortcut = $WshShell.CreateShortcut($DESKTOP_SHORTCUT)
    $Shortcut.TargetPath = "$INSTALL_DIR\launcher.bat"
    $Shortcut.WorkingDirectory = $INSTALL_DIR
    $Shortcut.Description = "Woodlanders Game Launcher"
    if (Test-Path "$INSTALL_DIR\launcher.ico") {
        $Shortcut.IconLocation = "$INSTALL_DIR\launcher.ico"
    }
    $Shortcut.Save()
    Write-Success "Desktop shortcut created"
    
    # Start Menu shortcut
    New-Item -ItemType Directory -Force -Path $START_MENU_DIR | Out-Null
    $StartMenuShortcut = $WshShell.CreateShortcut("$START_MENU_DIR\Woodlanders Launcher.lnk")
    $StartMenuShortcut.TargetPath = "$INSTALL_DIR\launcher.bat"
    $StartMenuShortcut.WorkingDirectory = $INSTALL_DIR
    $StartMenuShortcut.Description = "Woodlanders Game Launcher"
    if (Test-Path "$INSTALL_DIR\launcher.ico") {
        $StartMenuShortcut.IconLocation = "$INSTALL_DIR\launcher.ico"
    }
    $StartMenuShortcut.Save()
    Write-Success "Start Menu shortcut created"
}

function Show-CompletionMessage {
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Green
    Write-Host "  Installation Complete!" -ForegroundColor Green
    Write-Host "=========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Installed to: $INSTALL_DIR"
    Write-Host ""
    Write-Host "You can now launch $APP_NAME from:"
    Write-Host "  • Desktop shortcut"
    Write-Host "  • Start Menu > Woodlanders"
    Write-Host ""
    
    if (-not $Silent) {
        $launch = Read-Host "Launch now? (Y/n)"
        if ($launch -ne 'n' -and $launch -ne 'N') {
            Start-Process "$INSTALL_DIR\launcher.bat"
        }
    }
}

# Main installation process
try {
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host "  $APP_NAME Installer" -ForegroundColor Cyan
    Write-Host "  Version: $APP_VERSION" -ForegroundColor Cyan
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host ""
    
    # Check if Java is installed
    if (Test-JavaInstalled) {
        Write-Success "Java 17+ found on system"
    } else {
        if (-not (Download-JRE)) {
            throw "Failed to download and install JRE"
        }
    }
    
    # Install application
    if (-not (Install-Application)) {
        throw "Failed to install application"
    }
    
    # Create launcher script
    Create-LauncherScript
    
    # Create shortcuts
    Create-Shortcuts
    
    # Show completion message
    Show-CompletionMessage
    
} catch {
    Write-Host ""
    Write-Error-Message "Installation failed: $_"
    Write-Host ""
    if (-not $Silent) {
        Read-Host "Press Enter to exit"
    }
    exit 1
}
