package com.woodlanders.launcher.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodlanders.launcher.model.VersionMetadata;
import com.woodlanders.launcher.util.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionServiceTest {
    private final ObjectMapper mapper = ObjectMapperFactory.create();

    @Test
    void writesAndReadsMetadata() throws IOException {
        Path tempDir = Files.createTempDirectory("woodlanders-test-metadata");
        Path metadataPath = tempDir.resolve("version.json");
        VersionService service = new VersionService(mapper, metadataPath);

        VersionMetadata metadata = new VersionMetadata("v1.2.3", "deadbeef", Instant.parse("2025-11-23T00:00:00Z"), 42L);
        service.writeMetadata(metadata);

        Optional<VersionMetadata> loaded = service.readMetadata();
        assertTrue(loaded.isPresent(), "Metadata should be readable after writing");
        assertEquals(metadata.version(), loaded.get().version());
        assertEquals(metadata.sha256(), loaded.get().sha256());
        assertEquals(metadata.assetSize(), loaded.get().assetSize());
    }
}
