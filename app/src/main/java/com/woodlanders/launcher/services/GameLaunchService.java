package com.woodlanders.launcher.services;

import com.woodlanders.launcher.config.LauncherPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Spawns the actual Woodlanders Java client.
 */
public class GameLaunchService {
    private static final Logger LOG = LoggerFactory.getLogger(GameLaunchService.class);

    public Process launchClient() throws IOException {
        Path jarPath = LauncherPaths.clientJarPath();
        if (!Files.exists(jarPath)) {
            throw new IOException("Woodlanders client jar not found at " + jarPath);
        }
        
        // Build command with macOS-specific JVM arguments if needed
        ProcessBuilder builder;
        if (isMacOS()) {
            // On macOS, LWJGL/GLFW requires -XstartOnFirstThread for OpenGL applications
            builder = new ProcessBuilder("java", "-XstartOnFirstThread", "-jar", jarPath.toString());
            LOG.info("Launching Woodlanders on macOS with -XstartOnFirstThread from {}", jarPath);
        } else {
            builder = new ProcessBuilder("java", "-jar", jarPath.toString());
            LOG.info("Launching Woodlanders from {}", jarPath);
        }
        
        builder.directory(LauncherPaths.configDirectory().toFile());
        builder.inheritIO();
        
        // Debug: Log the exact command being executed
        LOG.info("Executing command: {}", String.join(" ", builder.command()));
        LOG.info("Working directory: {}", builder.directory());
        
        Process process = builder.start();
        LOG.info("Game process started with PID: {}", process.pid());
        return process;
    }
    
    /**
     * Detects if the current operating system is macOS.
     */
    private static boolean isMacOS() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("mac");
    }
}
