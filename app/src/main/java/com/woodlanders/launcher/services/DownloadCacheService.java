package com.woodlanders.launcher.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service for downloading and caching files with checksum verification.
 * Used by Gradle build tasks for downloading JDK and JavaFX.
 */
public class DownloadCacheService {
    
    /**
     * Result of a download operation.
     */
    public enum DownloadResult {
        CACHED,           // File was already cached with correct checksum
        DOWNLOADED,       // File was downloaded successfully
        CHECKSUM_MISMATCH // Cached file had incorrect checksum and was re-downloaded
    }
    
    /**
     * Checks if a file is cached with the correct checksum.
     * 
     * @param file The cached file to check
     * @param expectedSha256 The expected SHA256 checksum
     * @return true if file exists and checksum matches, false otherwise
     */
    public boolean isCachedWithCorrectChecksum(File file, String expectedSha256) {
        if (!file.exists()) {
            return false;
        }
        
        try {
            String actualChecksum = calculateSha256(file);
            return actualChecksum.equals(expectedSha256);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Downloads a file if not cached or if checksum doesn't match.
     * 
     * @param url The URL to download from
     * @param destination The destination file
     * @param expectedSha256 The expected SHA256 checksum
     * @return The result of the download operation
     * @throws IOException if download fails
     */
    public DownloadResult downloadIfNeeded(String url, File destination, String expectedSha256) throws IOException {
        // Check if already cached with correct checksum
        if (destination.exists()) {
            try {
                String actualChecksum = calculateSha256(destination);
                if (actualChecksum.equals(expectedSha256)) {
                    return DownloadResult.CACHED;
                } else {
                    // Checksum mismatch, delete and re-download
                    destination.delete();
                    download(url, destination);
                    verifyChecksum(destination, expectedSha256);
                    return DownloadResult.CHECKSUM_MISMATCH;
                }
            } catch (Exception e) {
                // If checksum calculation fails, re-download
                destination.delete();
            }
        }
        
        // Download the file
        download(url, destination);
        verifyChecksum(destination, expectedSha256);
        return DownloadResult.DOWNLOADED;
    }
    
    /**
     * Downloads a file from a URL.
     * 
     * @param url The URL to download from
     * @param destination The destination file
     * @throws IOException if download fails
     */
    private void download(String url, File destination) throws IOException {
        // Ensure parent directory exists
        destination.getParentFile().mkdirs();
        
        URL downloadUrl = new URL(url);
        URLConnection connection = downloadUrl.openConnection();
        connection.setRequestProperty("User-Agent", "Woodlanders-Launcher-Build");
        connection.connect();
        
        try (InputStream input = connection.getInputStream()) {
            Files.copy(input, destination.toPath());
        }
    }
    
    /**
     * Verifies the checksum of a file.
     * 
     * @param file The file to verify
     * @param expectedSha256 The expected SHA256 checksum
     * @throws IOException if checksum doesn't match
     */
    private void verifyChecksum(File file, String expectedSha256) throws IOException {
        try {
            String actualChecksum = calculateSha256(file);
            if (!actualChecksum.equals(expectedSha256)) {
                throw new IOException("Checksum mismatch! Expected: " + expectedSha256 + ", Actual: " + actualChecksum);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Calculates the SHA256 checksum of a file.
     * 
     * @param file The file to calculate checksum for
     * @return The SHA256 checksum as a hex string
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     * @throws IOException if file cannot be read
     */
    public String calculateSha256(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        
        try (InputStream input = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
