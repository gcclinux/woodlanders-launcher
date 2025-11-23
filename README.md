# Woodlanders Launcher (Java)

A desktop launcher for the [Woodlanders](https://gcclinux.github.io/Woodlanders/) game. It keeps the cached `woodlanders-client.jar` under `${HOME}/.config/woodlanders/` in sync with the latest GitHub release and provides a single-button JavaFX interface to launch or update.

## Features (Current Scaffold)
- Gradle-based Java 17 project with modular services for version tracking, GitHub release discovery, downloads, and process launching.
- JavaFX UI skeleton with state machine-driven primary action button.
- JSON metadata persistence backed by Jackson.
- Download + checksum utilities ready for integration with the live release flow.
- Unit test coverage for the metadata persistence service.

## Building & Testing
Requirements: JDK 17+ and Gradle (wrapper included after first build).

```bash
./gradlew test
```

Launch the desktop app (runs the JavaFX UI):

```bash
./gradlew :app:run
```

## Project Layout
```
app/
  src/main/java/com/woodlanders/launcher/
    config/LauncherPaths.java          # Resolves ~/.config/woodlanders paths
    model/*                            # Records describing releases, states, metadata
    services/*                         # GitHub, download, metadata, launcher services
    ui/LauncherApplication.java        # JavaFX entry point + UI wiring
    util/*                             # ObjectMapper + hashing helpers
  src/main/resources/application.css   # Minimal styling
specs/woodlanders-launcher.md           # Detailed project plan
```

## Snap Packaging
- `snapcraft.yaml` in the project root defines a strict snap using the Gradle plugin, staging OpenJDK 17 and JavaFX dependencies.
- Builds run `gradle :app:installDist` inside the part and expose the `woodlanders-launcher` command (wrapping `bin/app`).
- Required plugs (`desktop`, `wayland`, `x11`, `home`, `network`) are already declared; run `snapcraft` to build the snap locally.

## Roadmap
1. Flesh out GitHub release parsing + download workflow (hook UI button states to the services).
2. Add CLI flags for headless automation (optional for CI/testing).
3. Integrate logging pane + progress indicator in the UI.
4. Build & validate the snap under strict confinement.
5. Expand test suite (network client mocks, download manager, state machine scenarios).

---
Refer to `specs/woodlanders-launcher.md` for the end-to-end design goals and milestone breakdown.
