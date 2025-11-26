#!/bin/bash

# Set JAVA_HOME to the OpenJDK extension path
export JAVA_HOME=/usr/lib/sdk/openjdk21
export PATH=$JAVA_HOME/bin:$PATH

# Launch the application
# The application is installed in /app/woodlanders-launcher
# Gradle installDist creates the launcher script named after the project (app)
exec /app/woodlanders-launcher/bin/app "$@"
