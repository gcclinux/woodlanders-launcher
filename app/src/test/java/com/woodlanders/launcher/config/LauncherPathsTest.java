package com.woodlanders.launcher.config;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for LauncherPaths.
 */
class LauncherPathsTest {

    /**
     * Feature: macos-apple-silicon-package, Property 3: macOS path resolution
     * 
     * For any file operation in the launcher on macOS, the system SHALL use 
     * ~/Library/Application Support/Woodlanders/ for game files and configuration, 
     * and ~/Library/Caches/woodlanders-javafx/ for JavaFX cache, creating directories 
     * if they don't exist.
     * 
     * Validates: Requirements 4.1, 4.2, 4.3, 4.4
     */
    @Property(tries = 100)
    void macosPathResolution(@ForAll("osNames") String osName) {
        // Save original os.name
        String originalOsName = System.getProperty("os.name");
        
        try {
            // Set the os.name property to test different operating systems
            System.setProperty("os.name", osName);
            
            String home = System.getProperty("user.home");
            assertNotNull(home, "user.home should be set");
            
            Path configDir = LauncherPaths.configDirectory();
            Path javafxCacheDir = LauncherPaths.javafxCacheDirectory();
            
            // Determine if this OS name should be detected as macOS
            boolean shouldBeMacOS = osName.toLowerCase().contains("mac");
            
            if (shouldBeMacOS) {
                // On macOS, config should be in ~/Library/Application Support/Woodlanders/
                Path expectedConfig = Path.of(home, "Library/Application Support", "Woodlanders");
                assertEquals(expectedConfig, configDir, 
                    "macOS config directory should be ~/Library/Application Support/Woodlanders/");
                
                // On macOS, JavaFX cache should be in ~/Library/Caches/woodlanders-javafx/
                Path expectedCache = Path.of(home, "Library/Caches", "woodlanders-javafx");
                assertEquals(expectedCache, javafxCacheDir,
                    "macOS JavaFX cache directory should be ~/Library/Caches/woodlanders-javafx/");
            } else {
                // On non-macOS, config should be in ~/.config/woodlanders/
                Path expectedConfig = Path.of(home, ".config", "woodlanders");
                assertEquals(expectedConfig, configDir,
                    "Non-macOS config directory should be ~/.config/woodlanders/");
                
                // On non-macOS, JavaFX cache should be in ~/.cache/woodlanders-javafx/
                Path expectedCache = Path.of(home, ".cache", "woodlanders-javafx");
                assertEquals(expectedCache, javafxCacheDir,
                    "Non-macOS JavaFX cache directory should be ~/.cache/woodlanders-javafx/");
            }
            
            // Verify that clientJarPath and versionMetadataPath are relative to configDirectory
            Path clientJar = LauncherPaths.clientJarPath();
            Path versionMetadata = LauncherPaths.versionMetadataPath();
            
            assertTrue(clientJar.startsWith(configDir),
                "Client JAR path should be within config directory");
            assertTrue(versionMetadata.startsWith(configDir),
                "Version metadata path should be within config directory");
            
            assertEquals("woodlanders-client.jar", clientJar.getFileName().toString(),
                "Client JAR filename should be woodlanders-client.jar");
            assertEquals("version.json", versionMetadata.getFileName().toString(),
                "Version metadata filename should be version.json");
            
        } finally {
            // Restore original os.name
            System.setProperty("os.name", originalOsName);
        }
    }

    /**
     * Provides a variety of OS names for testing, including macOS variants and other platforms.
     */
    @Provide
    Arbitrary<String> osNames() {
        return Arbitraries.of(
            // macOS variants (should be detected as macOS)
            "Mac OS X",
            "macOS",
            "Darwin",
            "Mac OS",
            // Linux variants (should NOT be detected as macOS)
            "Linux",
            "linux",
            // Windows variants (should NOT be detected as macOS)
            "Windows 10",
            "Windows 11",
            "Windows",
            // Other Unix-like systems (should NOT be detected as macOS)
            "FreeBSD",
            "OpenBSD",
            "SunOS"
        );
    }

    /**
     * Feature: macos-apple-silicon-package, Property 3: macOS path resolution
     * 
     * Tests that ensureConfigDirectory creates both the config directory and 
     * JavaFX cache directory if they don't exist.
     * 
     * Validates: Requirements 4.3, 4.4
     */
    @Property(tries = 100)
    void directoryCreationWorks(@ForAll("osNames") String osName) throws IOException {
        // Save original os.name
        String originalOsName = System.getProperty("os.name");
        String originalHome = System.getProperty("user.home");
        
        try {
            // Create a temporary directory to use as fake home
            Path tempHome = Files.createTempDirectory("launcher-test-home");
            
            // Set system properties
            System.setProperty("os.name", osName);
            System.setProperty("user.home", tempHome.toString());
            
            // Get the expected directories
            Path configDir = LauncherPaths.configDirectory();
            Path javafxCacheDir = LauncherPaths.javafxCacheDirectory();
            
            // Ensure directories don't exist yet
            assertFalse(Files.exists(configDir), "Config directory should not exist before ensureConfigDirectory");
            assertFalse(Files.exists(javafxCacheDir), "JavaFX cache directory should not exist before ensureConfigDirectory");
            
            // Call ensureConfigDirectory
            LauncherPaths.ensureConfigDirectory();
            
            // Verify both directories were created
            assertTrue(Files.exists(configDir), "Config directory should exist after ensureConfigDirectory");
            assertTrue(Files.isDirectory(configDir), "Config path should be a directory");
            
            assertTrue(Files.exists(javafxCacheDir), "JavaFX cache directory should exist after ensureConfigDirectory");
            assertTrue(Files.isDirectory(javafxCacheDir), "JavaFX cache path should be a directory");
            
            // Clean up
            deleteRecursively(tempHome);
            
        } finally {
            // Restore original properties
            System.setProperty("os.name", originalOsName);
            System.setProperty("user.home", originalHome);
        }
    }

    /**
     * Helper method to recursively delete a directory.
     */
    private void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                try (var stream = Files.list(path)) {
                    stream.forEach(child -> {
                        try {
                            deleteRecursively(child);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            Files.delete(path);
        }
    }

    /**
     * Unit test to verify behavior on actual macOS system.
     */
    @Test
    void macosPathsOnActualMacOS() {
        String osName = System.getProperty("os.name");
        
        // Only run this test if we're actually on macOS
        if (osName != null && osName.toLowerCase().contains("mac")) {
            String home = System.getProperty("user.home");
            
            Path configDir = LauncherPaths.configDirectory();
            Path expectedConfig = Path.of(home, "Library/Application Support", "Woodlanders");
            assertEquals(expectedConfig, configDir);
            
            Path javafxCacheDir = LauncherPaths.javafxCacheDirectory();
            Path expectedCache = Path.of(home, "Library/Caches", "woodlanders-javafx");
            assertEquals(expectedCache, javafxCacheDir);
        }
    }
}
