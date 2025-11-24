@echo off
REM Woodlanders Launcher - Windows Batch Script
REM Version: ${VERSION}

setlocal

REM Find the directory where this script is located
set "APP_DIR=%~dp0"

REM Set JavaFX cache directory
set "JAVAFX_CACHE=%USERPROFILE%\.cache\woodlanders-javafx"

REM Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Java is not installed or not in PATH.
    echo Please install Java 17 or higher from https://adoptium.net/
    pause
    exit /b 1
)

REM Launch the application
echo Starting Woodlanders Launcher...
java -Djavafx.cachedir="%JAVAFX_CACHE%" -jar "%APP_DIR%woodlanders-launcher.jar"

if %ERRORLEVEL% neq 0 (
    echo.
    echo The application failed to start.
    echo Please make sure you have Java 17 or higher installed.
    pause
)

endlocal
