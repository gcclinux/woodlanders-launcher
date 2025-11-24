# Woodlanders Launcher

![Woodlanders Launcher](docs/woodlanders-launcher.png)

A desktop launcher for the [Woodlanders](https://gcclinux.github.io/Woodlanders/) game. This JavaFX application automatically checks for updates, downloads the latest game version from GitHub releases, and launches the game with a single click.

## Features

- ✅ **Automatic Updates** - Checks GitHub for the latest game version
- ✅ **Smart Caching** - Downloads and caches game files to `${HOME}/.config/woodlanders/`
- ✅ **Version Detection** - Compares local and remote versions using SHA-256 hashing
- ✅ **One-Click Launch** - Simple, intuitive interface to play the game
- ✅ **Cross-Platform** - Available for Windows, Linux (Snap/TAR.GZ)

## System Requirements

### Windows
- **OS**: Windows 10 or later
- **Java**: Not required (installer auto-downloads Java 21)
- **Disk Space**: ~200 MB (including Java runtime)

### Linux
- **OS**: Any modern Linux distribution
- **Java**: JRE 21+ (not needed for Snap package)
- **Disk Space**: ~200 MB

## Quick Start

### Windows (Recommended)

1. **Download** the installer package:
   - [`woodlanders-launcher-windows-installer-0.1.0.zip`](https://github.com/[your-repo]/woodlanders-launcher/releases/latest)

2. **Extract** and run `INSTALL.bat`

3. **Launch** from desktop shortcut or Start Menu

The installer will automatically download Java if you don't have it installed.

### Windows (Manual - Java Required)

1. **Download**: [`woodlanders-launcher-windows-0.1.0.zip`](https://github.com/[your-repo]/woodlanders-launcher/releases/latest)
2. **Require**: Java 21+ from [adoptium.net](https://adoptium.net/)
3. **Extract** and double-click `launcher.bat`

### Linux (Snap - Easiest)

```bash
# Install the snap package
sudo snap install --dangerous woodlanders-launcher_0.1.0_amd64.snap

# Grant necessary permissions
sudo snap connect woodlanders-launcher:home
sudo snap connect woodlanders-launcher:network

# Launch
snap run woodlanders-launcher
```

### Linux (TAR.GZ - Manual)

```bash
# Install Java if not already installed
sudo apt install openjdk-21-jre  # Debian/Ubuntu
# or
sudo dnf install java-21-openjdk  # Fedora

# Extract and run
tar -xzf woodlanders-launcher-linux-0.1.0.tar.gz
cd woodlanders-launcher-linux-0.1.0
chmod +x launcher.sh
./launcher.sh
```

## How It Works

1. **Launch** the launcher application
2. Application checks GitHub for the latest Woodlanders client release
3. If a new version is available, it's automatically downloaded
4. Click **"Launch Game"** to start playing
5. Game files are cached locally for faster subsequent launches

## Building from Source

See [`docs/BUILD_GUIDE.md`](docs/BUILD_GUIDE.md) for detailed build instructions.

```bash
# Build all distribution packages
gradle :app:buildAllPackages

# Run locally for testing
gradle run
```

## Configuration

Game files and cache are stored in:
- **Windows**: `%USERPROFILE%\.config\woodlanders\`
- **Linux**: `~/.config/woodlanders/`
- **JavaFX Cache**: `~/.cache/woodlanders-javafx/`

## Troubleshooting

### Windows: "Java is not recognized"
- The installer package automatically handles Java installation
- For manual installation, download Java from [adoptium.net](https://adoptium.net/)

### Linux: Application won't start
- Ensure Java 21+ is installed: `java -version`
- For Snap: Check permissions with `snap connections woodlanders-launcher`

### Game won't download
- Check your internet connection
- Verify GitHub is accessible
- Check logs in the application console

## Contributing

Contributions are welcome! Please read the build guide and submit pull requests.

## License

See [LICENSE](LICENSE) file for details.

## Links

- **Game Website**: [https://gcclinux.github.io/Woodlanders/](https://gcclinux.github.io/woodlanders/)
- **Game Repository**: [Woodlanders on GitHub](https://github.com/gcclinux/woodlanders)
- **Installation Guides**:
  - [Windows Installation Guide](WINDOWS_INSTALL_GUIDE.md)
  - [Build Guide](docs/BUILD_GUIDE.md)
