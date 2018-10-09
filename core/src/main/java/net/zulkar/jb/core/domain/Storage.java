package net.zulkar.jb.core.domain;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * File storage - any disk in windows, local, remote
 */
public interface Storage extends AutoCloseable {

    FileEntity[] listFiles(FileEntity dir) throws IOException;

    InputStream openInputStream(FileEntity entity) throws IOException;

    String getName();

}
