package com.woodlanders.launcher.model;

import java.net.URI;

/**
 * Minimal data extracted from GitHub for the latest release.
 */
public record ReleaseInfo(String tagName, URI downloadUrl, long assetSize) {
}
