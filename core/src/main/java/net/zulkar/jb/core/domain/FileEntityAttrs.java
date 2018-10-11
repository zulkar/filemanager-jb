package net.zulkar.jb.core.domain;

import java.time.LocalDateTime;

public class FileEntityAttrs {

    private final boolean isDir;
    private final boolean isContainer;
    private final long size;
    private final LocalDateTime modificationTime;

    public FileEntityAttrs(boolean isDir, boolean isContainer, long size, LocalDateTime modificationTime) {
        this.isDir = isDir;
        this.isContainer = isContainer;
        this.size = size;
        this.modificationTime = modificationTime;
    }

    public boolean isDir() {
        return isDir;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }
}
