package com.woodlanders.launcher.services;

import com.woodlanders.launcher.config.LauncherPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        
        configurePulseServer(builder.environment());
        
        builder.inheritIO();
        
        // Set audio environment for Snap confinement
        String snapName = System.getenv("SNAP_NAME");
        if (snapName != null) {
            builder.environment().put("PULSE_LATENCY_MSEC", "60");
        }
        
        // Debug: Log the exact command being executed
        LOG.info("Executing command: {}", String.join(" ", builder.command()));
        LOG.info("Working directory: {}", builder.directory());
        
        Process process = builder.start();
        LOG.info("Game process started with PID: {}", process.pid());
        return process;
    }
    
    private void configurePulseServer(Map<String, String> environment) {
        String inherited = System.getenv("PULSE_SERVER");
        if (isUsablePulseSocket(inherited)) {
            environment.put("PULSE_SERVER", inherited);
            LOG.info("Using inherited PulseAudio server {}", inherited);
            return;
        }
        
        List<Path> candidates = new ArrayList<>();
        String xdgRuntime = System.getenv("XDG_RUNTIME_DIR");
        if (xdgRuntime != null && !xdgRuntime.isBlank()) {
            Path runtimePath = Paths.get(xdgRuntime);
            candidates.add(runtimePath.resolve("pulse/native"));
            Path hostRuntime = runtimePath.getParent();
            if (hostRuntime != null) {
                candidates.add(hostRuntime.resolve("pulse/native"));
            }
        }
        String pulseRuntime = System.getenv("PULSE_RUNTIME_PATH");
        if (pulseRuntime != null && !pulseRuntime.isBlank()) {
            candidates.add(Paths.get(pulseRuntime, "native"));
        }
        String uid = System.getenv("UID");
        if (uid != null && !uid.isBlank()) {
            candidates.add(Paths.get("/run/user", uid, "pulse/native"));
        }
        for (Path candidate : candidates) {
            if (candidate != null && Files.exists(candidate)) {
                String value = "unix:" + candidate;
                environment.put("PULSE_SERVER", value);
                LOG.info("Configured PulseAudio socket at {}", candidate);
                return;
            }
        }
        LOG.warn("Could not locate a PulseAudio socket. Checked candidates: {}", candidates);
    }
    
    private boolean isUsablePulseSocket(String pulseServer) {
        if (pulseServer == null || pulseServer.isBlank() || !pulseServer.startsWith("unix:")) {
            return false;
        }
        Path socketPath = Paths.get(pulseServer.substring("unix:".length()));
        try {
            return Files.exists(socketPath);
        } catch (SecurityException e) {
            LOG.warn("Unable to access PulseAudio socket {}: {}", socketPath, e.getMessage());
            return false;
        }
    }
    
    /**
     * Detects if the current operating system is macOS.
     */
    private static boolean isMacOS() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("mac");
    }
}
