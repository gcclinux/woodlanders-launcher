# Woodlanders Launcher - PowerShell Script
# Version: ${VERSION}

# Find the directory where this script is located
$AppDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Set JavaFX cache directory
$JavaFXCache = Join-Path $env:USERPROFILE ".cache\woodlanders-javafx"

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
} catch {
    Write-Host "Java is not installed or not in PATH." -ForegroundColor Red
    Write-Host "Please install Java 17 or higher from https://adoptium.net/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Launch the application
Write-Host "Starting Woodlanders Launcher..." -ForegroundColor Green
$jarPath = Join-Path $AppDir "woodlanders-launcher.jar"

try {
    java "-Djavafx.cachedir=$JavaFXCache" -jar $jarPath
    if ($LASTEXITCODE -ne 0) {
        throw "Application exited with code $LASTEXITCODE"
    }
} catch {
    Write-Host ""
    Write-Host "The application failed to start." -ForegroundColor Red
    Write-Host "Please make sure you have Java 17 or higher installed." -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}
