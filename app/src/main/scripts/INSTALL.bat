@echo off
REM Woodlanders Launcher - Easy Installer
REM This script will install the launcher and download Java if needed

echo =========================================
echo   Woodlanders Launcher Installer
echo =========================================
echo.
echo This will install the Woodlanders Launcher on your system.
echo Java will be automatically downloaded if not found.
echo.
pause

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running with administrator privileges...
) else (
    echo Note: Running without administrator privileges.
    echo Installing to user directory.
)

echo.
echo Starting installation...
echo.

REM Run PowerShell installer
powershell.exe -ExecutionPolicy Bypass -File "%~dp0windows-installer.ps1"

if %ERRORLEVEL% equ 0 (
    echo.
    echo Installation completed successfully!
) else (
    echo.
    echo Installation failed. Please check the error messages above.
    pause
)
