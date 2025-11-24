#!/bin/bash
# Woodlanders Launcher - Linux Shell Script
# Version: ${VERSION}

# Find the directory where this script is located
APP_DIR="$(cd "$(dirname "$0")" && pwd)"

# Set JavaFX cache directory
JAVAFX_CACHE="${HOME}/.cache/woodlanders-javafx"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed or not in PATH."
    echo "Please install Java 17 or higher."
    exit 1
fi

# Launch the application
echo "Starting Woodlanders Launcher..."
java -Djavafx.cachedir="${JAVAFX_CACHE}" -jar "${APP_DIR}/woodlanders-launcher.jar"

exit_code=$?
if [ $exit_code -ne 0 ]; then
    echo ""
    echo "The application failed to start (exit code: $exit_code)."
    echo "Please make sure you have Java 17 or higher installed."
fi

exit $exit_code
