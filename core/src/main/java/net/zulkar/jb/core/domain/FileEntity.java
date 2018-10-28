package net.zulkar.jb.core.domain;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

public interface FileEntity {
    String getAbsolutePath();

    FileEntity getParent() throws IOException;

    List<FileEntity> ls() throws IOException;

    String getName();

    String getExtension();

    InputStream openInputStream() throws IOException;

    boolean isDir();

    boolean isContainer();

    long getSize();

    Instant getModificationTime();

}
