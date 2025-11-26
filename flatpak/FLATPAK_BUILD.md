# Flatpak Build & Test Commands

## Quick Reference

### Build
```bash
# Build
flatpak-builder --force-clean --repo=app/build/flatpak-repo app/build/flatpak-build flatpak/io.github.gcclinux.woodlanders.launcher.yml

# Create bundle
flatpak build-bundle app/build/flatpak-repo app/build/distributions/woodlanders-launcher.flatpak io.github.gcclinux.woodlanders.launcher

```

### Install/Update
```bash
# First time or after removing
flatpak --user remote-add --no-gpg-verify woodlanders-local app/build/flatpak-repo
flatpak --user install -y woodlanders-local io.github.gcclinux.woodlanders.launcher

# Update existing installation
flatpak run io.github.gcclinux.woodlanders.launcher
```

### Run
```bash
flatpak run com.woodlanders.launcher
```

### Uninstall
```bash
flatpak --user uninstall -y io.github.gcclinux.woodlanders.launcher || true
flatpak --user remote-delete woodlanders-local
```

### Clean Build
```bash
rm -rf .flatpak-builder app/build/flatpak-build app/build/flatpak-repo
```

## Current Issue

The Flatpak packaging works correctly, but there's a **Gradle build configuration issue**:

```
Error: Two versions of module com.fasterxml.jackson.datatype.jsr310 found in /app/woodlanders-launcher/lib
```

This is because `app/build.gradle` creates a fat JAR (with embedded dependencies) AND copies individual JARs to lib/.

### Fix: Don't build fat JAR for installDist

The Gradle `installDist` task already handles dependencies correctly by copying them to `lib/`. The fat JAR configuration conflicts with this.

In `app/build.gradle`, replace the `jar` task configuration with:

```gradle
jar {
    archiveBaseName = 'woodlanders-launcher'
    archiveVersion = ''
    manifest {
        attributes(
            'Main-Class': 'com.woodlanders.launcher.ui.LauncherApplication'
        )
    }
    // Don't create fat JAR - let installDist handle dependencies
}
```

Then rebuild both Gradle and Flatpak.
