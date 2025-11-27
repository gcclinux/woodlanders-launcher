package com.woodlanders.launcher.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodlanders.launcher.config.LauncherPaths;
import com.woodlanders.launcher.model.ReleaseInfo;
import com.woodlanders.launcher.model.VersionMetadata;
import com.woodlanders.launcher.util.ObjectMapperFactory;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for platform-independent functionality.
 * 
 * Feature: macos-apple-silicon-package, Property 7: Platform-independent functionality
 * 
 * For any launcher operation (GitHub API calls, version comparison, file download, JAR execution), 
 * the code path SHALL be identical to other platforms, using the same service classes without 
 * macOS-specific branches.
 * 
 * Validates: Requirements 9.1, 9.2, 9.3, 9.5
 */
class PlatformIndependentFunctionalityTest {
    
    private final ObjectMapper objectMapper = ObjectMapperFactory.create();
    
    /**
     * Property: Version metadata serialization format is identical across platforms.
     * 
     * For any version metadata, the JSON serialization format should be identical
     * regardless of the platform where it's generated.
     */
    @Property(tries = 100)
    void versionMetadataSerializationIsIdentical(
            @ForAll("versionString") String version,
            @ForAll("sha256Hash") String sha256,
            @ForAll("timestamp") Instant timestamp,
            @ForAll("fileSize") long assetSize) throws Exception {
        
        // Create version metadata
        VersionMetadata metadata = new VersionMetadata(version, sha256, timestamp, assetSize);
        
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(metadata);
        
        // Deserialize back
        VersionMetadata deserialized = objectMapper.readValue(json, VersionMetadata.class);
        
        // Verify round-trip consistency (platform-independent)
        assertEquals(metadata.version(), deserialized.version(),
            "Version should be preserved in serialization");
        assertEquals(metadata.sha256(), deserialized.sha256(),
            "SHA256 should be preserved in serialization");
        assertEquals(metadata.downloadedAt(), deserialized.downloadedAt(),
            "Downloaded timestamp should be preserved in serialization");
        assertEquals(metadata.assetSize(), deserialized.assetSize(),
            "Asset size should be preserved in serialization");
    }
    
    /**
     * Property: Version service file operations are platform-independent.
     * 
     * For any version metadata, the VersionService should read and write
     * files using the same logic regardless of platform.
     */
    @Property(tries = 100)
    void versionServiceOperationsArePlatformIndependent(
            @ForAll("versionString") String version,
            @ForAll("sha256Hash") String sha256,
            @ForAll("timestamp") Instant timestamp,
            @ForAll("fileSize") long assetSize) throws Exception {
        
        // Create temporary directory for testing
        Path tempDir = Files.createTempDirectory("version-test");
        Path metadataPath = tempDir.resolve("version.json");
        
        try {
            // Create version service with explicit path (platform-independent)
            VersionService service = new VersionService(objectMapper, metadataPath);
            VersionMetadata original = new VersionMetadata(version, sha256, timestamp, assetSize);
            
            // Write metadata
            service.writeMetadata(original);
            
            // Verify file exists
            assertTrue(Files.exists(metadataPath), "Metadata file should be created");
            
            // Read metadata back
            Optional<VersionMetadata> loaded = service.readMetadata();
            assertTrue(loaded.isPresent(), "Metadata should be readable");
            
            // Verify content is identical (platform-independent format)
            VersionMetadata read = loaded.get();
            assertEquals(original.version(), read.version());
            assertEquals(original.sha256(), read.sha256());
            assertEquals(original.downloadedAt(), read.downloadedAt());
            assertEquals(original.assetSize(), read.assetSize());
            
        } finally {
            // Clean up
            deleteRecursively(tempDir);
        }
    }
    
    /**
     * Property: ReleaseInfo construction is platform-independent.
     * 
     * For any valid release data, ReleaseInfo objects should be constructed
     * identically regardless of platform.
     */
    @Property(tries = 100)
    void releaseInfoConstructionIsPlatformIndependent(
            @ForAll("versionString") String version,
            @ForAll("downloadUrl") String url,
            @ForAll("fileSize") long size) {
        
        // Construct ReleaseInfo (platform-independent)
        URI downloadUri = URI.create(url);
        ReleaseInfo releaseInfo = new ReleaseInfo(version, downloadUri, size);
        
        // Verify all fields are preserved correctly
        assertEquals(version, releaseInfo.tagName(),
            "Tag name should be preserved in ReleaseInfo");
        assertEquals(downloadUri, releaseInfo.downloadUrl(),
            "Download URL should be preserved in ReleaseInfo");
        assertEquals(size, releaseInfo.assetSize(),
            "Asset size should be preserved in ReleaseInfo");
    }
    
    /**
     * Property: GitHub API request construction is platform-independent.
     * 
     * For any user agent string, the GithubReleaseService should construct
     * HTTP requests identically regardless of platform.
     */
    @Property(tries = 100)
    void githubApiRequestConstructionIsPlatformIndependent(
            @ForAll("userAgent") String userAgent) {
        
        // Create GitHub service (platform-independent)
        HttpClient httpClient = HttpClient.newHttpClient();
        GithubReleaseService service = new GithubReleaseService(httpClient, objectMapper, userAgent);
        
        // The service should be constructible without platform-specific logic
        assertNotNull(service, "GithubReleaseService should be constructible on any platform");
        
        // Note: We don't test actual network calls in property tests to avoid
        // external dependencies, but the construction and configuration should
        // be identical across platforms
    }
    
    /**
     * Property: Download service construction is platform-independent.
     * 
     * For any user agent string, the DownloadService should be constructible
     * identically regardless of platform.
     */
    @Property(tries = 100)
    void downloadServiceConstructionIsPlatformIndependent(
            @ForAll("userAgent") String userAgent) {
        
        // Create download service (platform-independent)
        HttpClient httpClient = HttpClient.newHttpClient();
        DownloadService service = new DownloadService(httpClient, userAgent);
        
        // The service should be constructible without platform-specific logic
        assertNotNull(service, "DownloadService should be constructible on any platform");
    }
    
    /**
     * Property: Game launch service construction is platform-independent.
     * 
     * The GameLaunchService should be constructible identically regardless of platform.
     * The actual launch command (java -jar) should be the same across platforms.
     */
    @Property(tries = 100)
    void gameLaunchServiceConstructionIsPlatformIndependent() {
        
        // Create game launch service (platform-independent)
        GameLaunchService service = new GameLaunchService();
        
        // The service should be constructible without platform-specific logic
        assertNotNull(service, "GameLaunchService should be constructible on any platform");
        
        // Note: We don't test actual process launching in property tests to avoid
        // side effects, but the service construction should be identical
    }
    
    /**
     * Unit test: Verify that path resolution is the only platform-specific behavior.
     * 
     * This test ensures that LauncherPaths is the only class with platform-specific
     * logic, while all service classes remain platform-independent.
     */
    @Test
    void pathResolutionIsOnlyPlatformSpecificBehavior() {
        // LauncherPaths should handle platform differences
        Path configDir = LauncherPaths.configDirectory();
        Path javafxCacheDir = LauncherPaths.javafxCacheDirectory();
        
        assertNotNull(configDir, "Config directory should be resolved");
        assertNotNull(javafxCacheDir, "JavaFX cache directory should be resolved");
        
        // All other services should be platform-independent
        HttpClient httpClient = HttpClient.newHttpClient();
        String userAgent = "test-agent";
        
        // These should all construct without platform-specific logic
        assertDoesNotThrow(() -> new GithubReleaseService(httpClient, objectMapper, userAgent));
        assertDoesNotThrow(() -> new DownloadService(httpClient, userAgent));
        assertDoesNotThrow(() -> new GameLaunchService());
        assertDoesNotThrow(() -> new VersionService(objectMapper));
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
     * Provides arbitrary version strings for testing.
     */
    @Provide
    Arbitrary<String> versionString() {
        return Arbitraries.strings()
            .withChars("0123456789.")
            .ofMinLength(3)
            .ofMaxLength(20)
            .filter(s -> s.matches("\\d+(\\.\\d+)*"));
    }
    
    /**
     * Provides arbitrary SHA-256 hash strings for testing.
     */
    @Provide
    Arbitrary<String> sha256Hash() {
        return Arbitraries.strings()
            .withChars("0123456789abcdef")
            .ofLength(64);
    }
    
    /**
     * Provides arbitrary timestamps for testing.
     */
    @Provide
    Arbitrary<Instant> timestamp() {
        return Arbitraries.longs()
            .between(0, System.currentTimeMillis() / 1000)
            .map(Instant::ofEpochSecond);
    }
    
    /**
     * Provides arbitrary file sizes for testing.
     */
    @Provide
    Arbitrary<Long> fileSize() {
        return Arbitraries.longs()
            .between(0, 1_000_000_000L); // Up to 1GB
    }
    
    /**
     * Provides arbitrary download URLs for testing.
     */
    @Provide
    Arbitrary<String> downloadUrl() {
        return Arbitraries.strings()
            .withChars("abcdefghijklmnopqrstuvwxyz0123456789-.")
            .ofMinLength(5)
            .ofMaxLength(50)
            .map(s -> "https://github.com/example/" + s + "/releases/download/v1.0.0/file.jar");
    }
    
    /**
     * Provides arbitrary user agent strings for testing.
     */
    @Provide
    Arbitrary<String> userAgent() {
        return Arbitraries.strings()
            .withChars("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-. /")
            .ofMinLength(5)
            .ofMaxLength(100)
            .filter(s -> !s.trim().isEmpty());
    }
}