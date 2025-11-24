# Woodlanders Launcher - Flatpak Build Guide

This guide explains how to build and install the Woodlanders Launcher as a Flatpak package.

## Prerequisites

You need to have `flatpak` and `flatpak-builder` installed on your system.

```bash
# Ubuntu/Debian
sudo apt install flatpak flatpak-builder

# Fedora
sudo dnf install flatpak flatpak-builder
```

You also need to install the Freedesktop SDK and the OpenJDK extension:

```bash
flatpak install org.freedesktop.Sdk//24.08
flatpak install org.freedesktop.Platform//24.08
flatpak install org.freedesktop.Sdk.Extension.openjdk21//24.08
```

## Building and Installing

Since this project uses Gradle, it requires network access during the build process to download dependencies. We use the `--share=network` flag.

Run the following command from the root of the repository:

```bash
flatpak-builder --share=network --user --install --force-clean build-dir flatpak/com.woodlanders.launcher.yml
```

## Running the Application

Once installed, you can run the application using:

```bash
flatpak run com.woodlanders.launcher
```

## Creating a Bundle (Single File)

To create a single-file `.flatpak` bundle that can be distributed:

1. Build the project (if not already done):
   ```bash
   flatpak-builder --share=network --repo=repo --force-clean build-dir flatpak/com.woodlanders.launcher.yml
   ```

2. Create the bundle:
   ```bash
   flatpak build-bundle repo woodlanders-launcher.flatpak com.woodlanders.launcher
   ```

You can then install this bundle on other machines using:
```bash
flatpak install woodlanders-launcher.flatpak
```
