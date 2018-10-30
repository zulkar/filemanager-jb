package net.zulkar.jb.core.domain;


import java.io.IOException;

/**
 * File storage - any disk in windows, local, remote
 */
public interface Storage extends AutoCloseable {
    FileEntity resolve(String path) throws IOException;

    String getName();

    FileEntity getRootEntity() throws IOException;

    default String getSystemInternalPath(FileEntity entity) {
        return entity.getAbsolutePath();
    }
}
