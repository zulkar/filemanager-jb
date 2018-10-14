package net.zulkar.jb.core.domain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

public interface FileEntity {
    String getAbsolutePath();

    FileEntity getParent();

    List<FileEntity> ls();

    String getName();

    String getExtension();

    InputStream openInputStream() throws IOException;

    boolean isDir();

    boolean isContainer();

    long getSize();

    LocalDateTime getModificationTime();
}
