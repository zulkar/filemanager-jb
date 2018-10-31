package net.zulkar.jb.core.handlers.zip.archives;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.ProxyFileEntity;
import net.zulkar.jb.core.handlers.zip.ZipArchive;

import java.io.IOException;
import java.util.List;

public class BadZipArchive extends ProxyFileEntity implements ZipArchive {
    private final IOException exception;

    public BadZipArchive(FileEntity entity, IOException exception) {
        super(entity);
        this.exception = exception;
    }

    @Override
    public List<FileEntity> ls() throws IOException {
        throw new IOException(String.format("Cannot ls %s : %s", entity.getAbsolutePath(), exception.getMessage()), exception);
    }
}
