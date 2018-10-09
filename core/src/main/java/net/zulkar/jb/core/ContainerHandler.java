package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public interface ContainerHandler {
    boolean maySupport(Storage storage, File file);
    InputStream readContent(Storage storage, File containerFile, FileEntity entity) throws UncheckedIOException;
    FileEntity[] listFiles(Storage storage, File containerFile, FileEntity dir);
}
