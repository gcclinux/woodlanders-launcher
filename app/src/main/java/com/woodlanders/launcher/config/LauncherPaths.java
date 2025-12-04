package com.woodlanders.launcher.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Centralizes all filesystem locations used by the launcher.
 */
public final class LauncherPaths {
    private static final String CONFIG_ROOT = ".config";
    private static final String APP_DIR = "woodlanders";
    private static final String MACOS_APP_SUPPORT = "Library/Application Support";
    private static final String MACOS_CACHES = "Library/Caches";
    private static final String JAVAFX_CACHE_DIR = "woodlanders-javafx";
    private static final String CLIENT_JAR = "woodlanders-client.jar";
    private static final String VERSION_FILE = "version.json";

    private LauncherPaths() {
    }

    /**
     * Detects if the current operating system is macOS.
     */
    private static boolean isMacOS() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("mac");
    }

    /**
     * Detects if running inside a snap container.
     */
    @SuppressWarnings("unused")
    private static boolean isSnap() {
        return System.getenv("SNAP_USER_COMMON") != null;
    }

    public static Path configDirectory() {
        // When running as a snap, use SNAP_USER_COMMON for persistent storage
        String snapUserCommon = System.getenv("SNAP_USER_COMMON");
        if (snapUserCommon != null) {
            return Path.of(snapUserCommon, APP_DIR);
        }
        
        String home = System.getProperty("user.home");
        Objects.requireNonNull(home, "user.home system property is missing");
        
        if (isMacOS()) {
            return Path.of(home, MACOS_APP_SUPPORT, "Woodlanders");
        }
        
        return Path.of(home, CONFIG_ROOT, APP_DIR);
    }

    /**
     * Returns the JavaFX cache directory path.
     * On macOS: ~/Library/Caches/woodlanders-javafx/
     * On other platforms: ~/.cache/woodlanders-javafx/
     */
    public static Path javafxCacheDirectory() {
        String home = System.getProperty("user.home");
        Objects.requireNonNull(home, "user.home system property is missing");
        
        if (isMacOS()) {
            return Path.of(home, MACOS_CACHES, JAVAFX_CACHE_DIR);
        }
        
        return Path.of(home, ".cache", JAVAFX_CACHE_DIR);
    }

    public static Path clientJarPath() {
        return configDirectory().resolve(CLIENT_JAR);
    }

    public static Path versionMetadataPath() {
        return configDirectory().resolve(VERSION_FILE);
    }

    public static Path tempDownloadPath() throws IOException {
        return Files.createTempFile(configDirectory(), "woodlanders-client", ".tmp");
    }

    public static void ensureConfigDirectory() throws IOException {
        Files.createDirectories(configDirectory());
        Files.createDirectories(javafxCacheDirectory());
    }
}
