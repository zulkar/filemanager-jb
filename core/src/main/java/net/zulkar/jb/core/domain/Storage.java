package net.zulkar.jb.core.domain;


import java.io.IOException;

/**
 * File storage - any disk in windows, local, remote
 */
public interface Storage extends AutoCloseable {
    FileEntity resolve(String path) throws IOException;

    String getName();

    FileEntity getRootEntity() throws IOException;

    boolean needCache();

    default String getsysteminternalpath(FileEntity entity) {
        return entity.getAbsolutePath();
    }
}
