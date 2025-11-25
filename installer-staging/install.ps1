# Woodlanders Launcher - Windows Installer
param([switch]$Silent = $false)
$ErrorActionPreference = "Stop"

$APP_NAME = "Woodlanders Launcher"
$APP_VERSION = "LOCAL-BUILD"

# Resolve key user folders robustly to avoid null Path arguments
$localAppData = [Environment]::GetFolderPath('LocalApplicationData')
$userProfile  = [Environment]::GetFolderPath('UserProfile')
$appData      = [Environment]::GetFolderPath('ApplicationData')

if (-not $localAppData) { throw "LOCALAPPDATA could not be resolved" }
if (-not $userProfile)  { throw "USERPROFILE could not be resolved" }
if (-not $appData)      { throw "APPDATA could not be resolved" }

$INSTALL_DIR      = Join-Path $localAppData 'Woodlanders\Launcher'
$JRE_DIR          = Join-Path $INSTALL_DIR 'jre'
$DESKTOP_SHORTCUT = Join-Path (Join-Path $userProfile 'Desktop') 'Woodlanders Launcher.lnk'
$START_MENU_DIR   = Join-Path $appData 'Microsoft\Windows\Start Menu\Programs\Woodlanders'
$JRE_DOWNLOAD_URL = "https://download.bell-sw.com/java/21.0.5+11/bellsoft-jdk21.0.5+11-windows-amd64-full.zip"

function Write-Status { param([string]$Message); if (-not $Silent) { Write-Host $Message -ForegroundColor Cyan } }
function Write-Success { param([string]$Message); if (-not $Silent) { Write-Host "OK $Message" -ForegroundColor Green } }
function Write-ErrorMsg { param([string]$Message); Write-Host "ERROR $Message" -ForegroundColor Red }

function Test-JavaInstalled {
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
        if ($javaVersion -match "(\d+)\.(\d+)") {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -ge 17) { return $true }
        }
    } catch { return $false }
    return $false
}

function Download-JRE {
    Write-Status "Downloading JDK with JavaFX..."
    if (Test-Path "$JRE_DIR\bin\java.exe") { Write-Success "JDK already installed"; return $true }
    if (Test-Path $JRE_DIR) { Remove-Item $JRE_DIR -Recurse -Force -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Force -Path $JRE_DIR | Out-Null
    $jreZip = "$env:TEMP\woodlanders-jre.zip"
    try {
        Write-Status "Downloading JDK (this may take a few minutes)..."
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $JRE_DOWNLOAD_URL -OutFile $jreZip -UseBasicParsing
        $ProgressPreference = 'Continue'
        Write-Status "Extracting JDK..."
        $tempExtract = "$env:TEMP\woodlanders-jre-extract"
        if (Test-Path $tempExtract) { Remove-Item $tempExtract -Recurse -Force }
        
        # Try Expand-Archive first, fallback to .NET if module not available
        try {
            Expand-Archive -Path $jreZip -DestinationPath $tempExtract -Force -ErrorAction Stop
        } catch {
            Write-Status "Using fallback extraction method..."
            Add-Type -AssemblyName System.IO.Compression.FileSystem
            [System.IO.Compression.ZipFile]::ExtractToDirectory($jreZip, $tempExtract)
        }
        
        $extractedDir = Get-ChildItem -Path $tempExtract -Directory | Select-Object -First 1
        Get-ChildItem -Path $extractedDir.FullName | Move-Item -Destination $JRE_DIR -Force
        Remove-Item $tempExtract -Recurse -Force
        Remove-Item $jreZip -Force
        Write-Success "JDK installed"
        return $true
    } catch {
        Write-ErrorMsg "Failed to download JDK: $_"
        return $false
    }
}

function Install-Application {
    Write-Status "Installing $APP_NAME..."
    New-Item -ItemType Directory -Force -Path $INSTALL_DIR | Out-Null
    $scriptDir = if ($PSScriptRoot) { $PSScriptRoot } elseif ($MyInvocation.MyCommand.Path) { Split-Path -Parent $MyInvocation.MyCommand.Path } else { Get-Location | Select-Object -ExpandProperty Path }
    if (Test-Path "$scriptDir\woodlanders-launcher.jar") {
        Copy-Item "$scriptDir\woodlanders-launcher.jar" -Destination $INSTALL_DIR -Force
        Write-Success "Application files copied"
    } else {
        Write-ErrorMsg "Application JAR not found at $scriptDir\woodlanders-launcher.jar"
        return $false
    }
    if (Test-Path "$scriptDir\launcher-icon.png") {
        Copy-Item "$scriptDir\launcher-icon.png" -Destination $INSTALL_DIR -Force
    }
    return $true
}

function New-LauncherScript {
    $batContent = "@echo off`r`nsetlocal`r`nset APP_DIR=%~dp0`r`nset JAVAFX_CACHE=%USERPROFILE%\.cache\woodlanders-javafx`r`nif exist %APP_DIR%jre\bin\java.exe (set JAVA_CMD=%APP_DIR%jre\bin\java.exe) else (set JAVA_CMD=java)`r`n%JAVA_CMD% -Djavafx.cachedir=%JAVAFX_CACHE% -jar %APP_DIR%woodlanders-launcher.jar`r`nif %ERRORLEVEL% neq 0 (echo. & echo Application failed to start. & pause)`r`nendlocal"
    $batContent | Out-File -FilePath "$INSTALL_DIR\launcher.bat" -Encoding ASCII -Force
    Write-Success "Launcher script created"
}

function New-Shortcuts {
    if (-not $DESKTOP_SHORTCUT -or -not $START_MENU_DIR -or -not $INSTALL_DIR) {
        Write-ErrorMsg "Shortcut paths not initialized correctly. Desktop='$DESKTOP_SHORTCUT' StartMenu='$START_MENU_DIR' InstallDir='$INSTALL_DIR'"
        return
    }
    $WshShell = New-Object -ComObject WScript.Shell
    $Shortcut = $WshShell.CreateShortcut($DESKTOP_SHORTCUT)
    $Shortcut.TargetPath = "$INSTALL_DIR\launcher.bat"
    $Shortcut.WorkingDirectory = $INSTALL_DIR
    $Shortcut.Description = "Woodlanders Game Launcher"
    if (Test-Path "$INSTALL_DIR\launcher-icon.png") {
        $Shortcut.IconLocation = "$INSTALL_DIR\launcher-icon.png"
    }
    $Shortcut.Save()
    Write-Success "Desktop shortcut created"
    if (-not (Test-Path $START_MENU_DIR)) {
        New-Item -ItemType Directory -Force -Path $START_MENU_DIR | Out-Null
    }
    $StartMenuShortcut = $WshShell.CreateShortcut("$START_MENU_DIR\Woodlanders Launcher.lnk")
    $StartMenuShortcut.TargetPath = "$INSTALL_DIR\launcher.bat"
    $StartMenuShortcut.WorkingDirectory = $INSTALL_DIR
    $StartMenuShortcut.Description = "Woodlanders Game Launcher"
    if (Test-Path "$INSTALL_DIR\launcher-icon.png") {
        $StartMenuShortcut.IconLocation = "$INSTALL_DIR\launcher-icon.png"
    }
    $StartMenuShortcut.Save()
    Write-Success "Start Menu shortcut created"
}

try {
    Write-Host ""
    Write-Host "Woodlanders Launcher Installer" -ForegroundColor Cyan
    Write-Host "Version: $APP_VERSION" -ForegroundColor Cyan
    Write-Host ""
    if (Test-JavaInstalled) {
        Write-Success "Java 17+ found"
    } else {
        if (-not (Download-JRE)) { throw "Failed to install JRE" }
    }
    if (-not (Install-Application)) { throw "Failed to install application" }
    New-LauncherScript
    New-Shortcuts
    Write-Host ""
    Write-Host "Installation Complete!" -ForegroundColor Green
    Write-Host "Installed to: $INSTALL_DIR"
    Write-Host ""
    if (-not $Silent) {
        $launch = Read-Host "Launch now? (Y/n)"
        if ($launch -ne 'n' -and $launch -ne 'N') {
            Start-Process -FilePath "$INSTALL_DIR\launcher.bat"
        }
    }
} catch {
    Write-Host ""
    Write-ErrorMsg "Installation failed: $_"
    if (-not $Silent) { Read-Host "Press Enter to exit" }
    exit 1
}

# # Always pause at the end so user can see the output
# if (-not $Silent) {
#     Write-Host ""
#     Read-Host "Press Enter to close this window"
# }
