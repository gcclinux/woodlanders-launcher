package com.woodlanders.launcher.services;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for DownloadCacheService.
 * 
 * Feature: macos-apple-silicon-package, Property 6: Download caching behavior
 * 
 * For any build execution, if JDK or JavaFX archives already exist in the cache 
 * directory with correct checksums, the system SHALL skip downloading and reuse 
 * cached files; if missing or checksum mismatch, SHALL download fresh copies.
 * 
 * Validates: Requirements 5.1, 5.2
 */
class DownloadCacheServiceTest {
    
    private final DownloadCacheService service = new DownloadCacheService();
    
    /**
     * Property: For any file with correct checksum, isCachedWithCorrectChecksum returns true.
     */
    @Property(tries = 100)
    void cachedFileWithCorrectChecksumIsDetected(
            @ForAll("fileContent") byte[] content) throws Exception {
        
        // Create a temporary directory and file
        Path tempDir = Files.createTempDirectory("test-cache");
        try {
            File file = tempDir.resolve("test-file.bin").toFile();
            Files.write(file.toPath(), content);
            
            // Calculate the actual checksum
            String actualChecksum = service.calculateSha256(file);
            
            // Verify that isCachedWithCorrectChecksum returns true
            assertTrue(service.isCachedWithCorrectChecksum(file, actualChecksum),
                "File with correct checksum should be detected as cached");
        } finally {
            deleteRecursively(tempDir);
        }
    }
    
    /**
     * Property: For any file with incorrect checksum, isCachedWithCorrectChecksum returns false.
     */
    @Property(tries = 100)
    void cachedFileWithIncorrectChecksumIsDetected(
            @ForAll("fileContent") byte[] content,
            @ForAll("sha256Hash") String wrongChecksum) throws Exception {
        
        // Create a temporary directory and file
        Path tempDir = Files.createTempDirectory("test-cache");
        try {
            File file = tempDir.resolve("test-file.bin").toFile();
            Files.write(file.toPath(), content);
            
            // Calculate the actual checksum
            String actualChecksum = service.calculateSha256(file);
            
            // Skip if by chance the wrong checksum matches the actual one
            Assume.that(!actualChecksum.equals(wrongChecksum));
            
            // Verify that isCachedWithCorrectChecksum returns false
            assertFalse(service.isCachedWithCorrectChecksum(file, wrongChecksum),
                "File with incorrect checksum should not be detected as cached");
        } finally {
            deleteRecursively(tempDir);
        }
    }
    
    /**
     * Property: For any non-existent file, isCachedWithCorrectChecksum returns false.
     */
    @Property(tries = 100)
    void nonExistentFileIsNotCached(
            @ForAll("sha256Hash") String checksum) throws Exception {
        
        Path tempDir = Files.createTempDirectory("test-cache");
        try {
            File file = tempDir.resolve("non-existent-file.bin").toFile();
            
            // Verify file doesn't exist
            assertFalse(file.exists(), "File should not exist");
            
            // Verify that isCachedWithCorrectChecksum returns false
            assertFalse(service.isCachedWithCorrectChecksum(file, checksum),
                "Non-existent file should not be detected as cached");
        } finally {
            deleteRecursively(tempDir);
        }
    }
    
    /**
     * Property: Checksum calculation is deterministic - same file produces same checksum.
     */
    @Property(tries = 100)
    void checksumCalculationIsDeterministic(
            @ForAll("fileContent") byte[] content) throws Exception {
        
        Path tempDir = Files.createTempDirectory("test-cache");
        try {
            File file = tempDir.resolve("test-file.bin").toFile();
            Files.write(file.toPath(), content);
            
            // Calculate checksum twice
            String checksum1 = service.calculateSha256(file);
            String checksum2 = service.calculateSha256(file);
            
            // Verify they are the same
            assertEquals(checksum1, checksum2,
                "Checksum calculation should be deterministic");
        } finally {
            deleteRecursively(tempDir);
        }
    }
    
    /**
     * Property: Different content produces different checksums (collision resistance).
     */
    @Property(tries = 100)
    void differentContentProducesDifferentChecksums(
            @ForAll("fileContent") byte[] content1,
            @ForAll("fileContent") byte[] content2) throws Exception {
        
        // Skip if contents are the same
        Assume.that(!java.util.Arrays.equals(content1, content2));
        
        Path tempDir = Files.createTempDirectory("test-cache");
        try {
            // Create two files with different content
            File file1 = tempDir.resolve("file1.bin").toFile();
            File file2 = tempDir.resolve("file2.bin").toFile();
            Files.write(file1.toPath(), content1);
            Files.write(file2.toPath(), content2);
            
            // Calculate checksums
            String checksum1 = service.calculateSha256(file1);
            String checksum2 = service.calculateSha256(file2);
            
            // Verify they are different
            assertNotEquals(checksum1, checksum2,
                "Different content should produce different checksums");
        } finally {
            deleteRecursively(tempDir);
        }
    }
    
    /**
     * Unit test: Verify checksum format is valid SHA-256 (64 hex characters).
     */
    @Test
    void checksumFormatIsValid(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("test.txt").toFile();
        Files.writeString(file.toPath(), "test content");
        
        String checksum = service.calculateSha256(file);
        
        // SHA-256 produces 64 hex characters
        assertEquals(64, checksum.length(), "SHA-256 checksum should be 64 characters");
        assertTrue(checksum.matches("[0-9a-f]{64}"), "Checksum should be lowercase hex");
    }
    
    /**
     * Unit test: Verify known SHA-256 checksum for empty file.
     */
    @Test
    void emptyFileHasKnownChecksum(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("empty.txt").toFile();
        Files.write(file.toPath(), new byte[0]);
        
        String checksum = service.calculateSha256(file);
        
        // SHA-256 of empty file is known
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", 
            checksum, "Empty file should have known SHA-256 checksum");
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
     * Provides arbitrary file content for testing.
     */
    @Provide
    Arbitrary<byte[]> fileContent() {
        return Arbitraries.bytes()
            .array(byte[].class)
            .ofMinSize(0)
            .ofMaxSize(1024); // Keep files small for testing
    }
    
    /**
     * Provides arbitrary SHA-256 hash strings for testing.
     */
    @Provide
    Arbitrary<String> sha256Hash() {
        // Generate 64 hex characters (0-9, a-f)
        return Arbitraries.strings()
            .withChars("0123456789abcdef")
            .ofLength(64);
    }
}
