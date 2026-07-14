package com.fongmi.android.tv.ai.subtitle;

public final class ModelFile {
    public final String relativePath;
    public final String url;
    public final long size;
    public final String sha256;

    public ModelFile(String relativePath, String url, long size) {
        this(relativePath, url, size, "");
    }

    public ModelFile(String relativePath, String url, long size, String sha256) {
        this.relativePath = relativePath;
        this.url = url;
        this.size = size;
        this.sha256 = sha256 == null ? "" : sha256;
    }
}
