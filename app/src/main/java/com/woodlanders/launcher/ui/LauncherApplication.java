package com.woodlanders.launcher.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodlanders.launcher.config.LauncherPaths;
import com.woodlanders.launcher.model.DownloadResult;
import com.woodlanders.launcher.model.LauncherModel;
import com.woodlanders.launcher.model.LauncherState;
import com.woodlanders.launcher.model.ReleaseInfo;
import com.woodlanders.launcher.model.VersionMetadata;
import com.woodlanders.launcher.services.DownloadService;
import com.woodlanders.launcher.services.GameLaunchService;
import com.woodlanders.launcher.services.GithubReleaseService;
import com.woodlanders.launcher.services.VersionService;
import com.woodlanders.launcher.util.ObjectMapperFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Minimal JavaFX front-end that wires services together.
 */
public class LauncherApplication extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(LauncherApplication.class);
    private static final String USER_AGENT = "woodlanders-launcher/0.1.0 (+https://gcclinux.github.io/Woodlanders/)";
    private static final URI PROJECT_URL = URI.create("https://gcclinux.github.io/Woodlanders/");
    private static final Path JAVAFX_CACHE_DIR = determineJavaFxCacheDir();

    static {
        try {
            Files.createDirectories(JAVAFX_CACHE_DIR);
            LOG.info("JavaFX cache directory set to {}", JAVAFX_CACHE_DIR);
        } catch (IOException e) {
            LOG.warn("Failed to initialize JavaFX cache directory at {}", JAVAFX_CACHE_DIR, e);
        }
    }

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final GithubReleaseService githubService = new GithubReleaseService(httpClient, objectMapper, USER_AGENT);
    private final VersionService versionService = new VersionService(objectMapper);
    private final DownloadService downloadService = new DownloadService(httpClient, USER_AGENT);
    private final GameLaunchService gameLaunchService = new GameLaunchService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new LauncherThreadFactory());

    private LauncherModel currentModel = LauncherModel.checking();
    private Optional<ReleaseInfo> currentRelease = Optional.empty();
    private Optional<VersionMetadata> currentMetadata = Optional.empty();

    private Button primaryButton;
    private Label statusLabel;
    private Label versionLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Woodlanders Launcher");

        Label heading = new Label("Woodlanders Launcher");
        heading.getStyleClass().add("launcher-title");

        statusLabel = new Label(currentModel.message());
        statusLabel.getStyleClass().add("launcher-status");

        versionLabel = new Label(formatVersionText(currentModel));
        versionLabel.getStyleClass().add("launcher-version");

        primaryButton = new Button(currentModel.primaryActionLabel());
        primaryButton.setDisable(!currentModel.primaryActionEnabled());
        primaryButton.setOnAction(event -> handlePrimaryAction());
        primaryButton.setMaxWidth(Double.MAX_VALUE);

        Hyperlink websiteLink = new Hyperlink("Project site: gcclinux.github.io/Woodlanders");
        websiteLink.setOnAction(event -> getHostServices().showDocument(PROJECT_URL.toString()));

        VBox root = new VBox(12, heading, statusLabel, primaryButton, versionLabel, websiteLink);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setFillWidth(true);

        Scene scene = new Scene(root, 460, 260);
        String cssPath = getClass().getResource("/application.css") != null
                ? Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }
        stage.setScene(scene);
        stage.show();

        refreshState();
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    private void refreshState() {
        Platform.runLater(() -> applyModel(LauncherModel.checking()));
        executor.submit(() -> {
            LauncherModel evaluated = evaluateState();
            Platform.runLater(() -> applyModel(evaluated));
        });
    }

    private LauncherModel evaluateState() {
        Optional<VersionMetadata> localMeta = versionService.readMetadata();
        boolean jarExists = java.nio.file.Files.exists(LauncherPaths.clientJarPath());
        Optional<ReleaseInfo> latest = githubService.fetchLatestRelease();
        currentRelease = latest;
        currentMetadata = localMeta;

        if (latest.isEmpty()) {
            if (jarExists) {
                return buildModel(LauncherState.OFFLINE_READY, "Launch Offline", true,
                        "Offline mode: using cached client.",
                        localMeta.map(VersionMetadata::version).orElse("cached"), "unknown");
            }
            return buildModel(LauncherState.BLOCKED, "Retry", true,
                    "Unable to reach GitHub. Connect to the internet and retry.",
                    "missing", "unknown");
        }

        ReleaseInfo releaseInfo = latest.get();
        String remoteVersion = releaseInfo.tagName();
        String localVersion = localMeta.map(VersionMetadata::version).orElse(jarExists ? "cached" : "missing");
        boolean versionsMatch = jarExists && localMeta.map(meta -> remoteVersion.equals(meta.version())).orElse(false);

        if (versionsMatch) {
            return buildModel(LauncherState.READY_TO_LAUNCH, "Launch Woodlanders", true,
                    "Latest version " + remoteVersion + " is ready.", localVersion, remoteVersion);
        }

        String label = jarExists ? "Update to " + remoteVersion : "Download " + remoteVersion;
        String message = jarExists ? "A newer build is available." : "Download required before first launch.";
        return buildModel(LauncherState.NEEDS_UPDATE, label, true, message, localVersion, remoteVersion);
    }

    private void handlePrimaryAction() {
        switch (currentModel.state()) {
            case READY_TO_LAUNCH, OFFLINE_READY -> launchClient();
            case NEEDS_UPDATE -> downloadLatest();
            case BLOCKED, ERROR -> refreshState();
            default -> LOG.debug("Ignoring action for state {}", currentModel.state());
        }
    }

    private void downloadLatest() {
        ReleaseInfo release = currentRelease.orElse(null);
        if (release == null) {
            applyModel(buildModel(LauncherState.ERROR, "Retry", true,
                    "Release metadata unavailable. Please retry the check.",
                    currentModel.localVersion(), currentModel.remoteVersion()));
            return;
        }
        applyModel(buildModel(LauncherState.UPDATING, "Downloading…", false,
                "Downloading " + release.tagName() + "…",
                currentModel.localVersion(), release.tagName()));
        executor.submit(() -> {
            try {
                DownloadResult result = downloadService.downloadRelease(release);
                VersionMetadata metadata = new VersionMetadata(release.tagName(), result.sha256(), Instant.now(), result.size());
                versionService.writeMetadata(metadata);
                currentMetadata = Optional.of(metadata);
                applyLater(buildModel(LauncherState.READY_TO_LAUNCH, "Launch Woodlanders", true,
                        "Updated to " + release.tagName() + ".",
                        metadata.version(), release.tagName()));
            } catch (IOException e) {
                LOG.error("Download failed", e);
                applyLater(buildModel(LauncherState.ERROR, "Retry", true,
                        "Download failed: " + e.getMessage(),
                        currentModel.localVersion(), release.tagName()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.warn("Download interrupted", e);
                applyLater(buildModel(LauncherState.ERROR, "Retry", true,
                        "Download interrupted. Retry when ready.",
                        currentModel.localVersion(), release.tagName()));
            }
        });
    }

    private void launchClient() {
        applyModel(buildModel(LauncherState.LAUNCHING, "Launching…", false,
                "Starting Woodlanders client…",
                currentModel.localVersion(), currentModel.remoteVersion()));
        executor.submit(() -> {
            try {
                gameLaunchService.launchClient();
                applyLater(buildModel(LauncherState.READY_TO_LAUNCH, "Launch Woodlanders", true,
                        "Client launched. Use this window for updates.",
                        currentMetadata.map(VersionMetadata::version).orElse(currentModel.localVersion()),
                        currentModel.remoteVersion()));
            } catch (IOException e) {
                LOG.error("Failed to launch client", e);
                applyLater(buildModel(LauncherState.ERROR, "Retry", true,
                        "Launch failed: " + e.getMessage(),
                        currentModel.localVersion(), currentModel.remoteVersion()));
            }
        });
    }

    private void applyModel(LauncherModel model) {
        this.currentModel = model;
        primaryButton.setText(model.primaryActionLabel());
        primaryButton.setDisable(!model.primaryActionEnabled());
        statusLabel.setText(model.message());
        versionLabel.setText(formatVersionText(model));
    }

    private void applyLater(LauncherModel model) {
        Platform.runLater(() -> applyModel(model));
    }

    private LauncherModel buildModel(LauncherState state, String actionLabel, boolean actionEnabled,
                                     String message, String localVersion, String remoteVersion) {
        return new LauncherModel(state, actionLabel, actionEnabled, message, localVersion, remoteVersion);
    }

    private String formatVersionText(LauncherModel model) {
        return String.format("Local: %s | Remote: %s", model.localVersion(), model.remoteVersion());
    }

    private static final class LauncherThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "woodlanders-launcher-worker");
            thread.setDaemon(true);
            return thread;
        }
    }

    private static Path determineJavaFxCacheDir() {
        String configured = System.getProperty("javafx.cachedir");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured);
        }
        String snapUserCommon = System.getenv("SNAP_USER_COMMON");
        if (snapUserCommon != null && !snapUserCommon.isBlank()) {
            return Path.of(snapUserCommon, "javafx-cache");
        }
        String userHome = System.getProperty("user.home", ".");
        return Path.of(userHome, ".cache", "woodlanders-javafx");
    }
}
