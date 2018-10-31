package net.zulkar.jb.core.handlers.zip;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.ProxyFileEntity;
import net.zulkar.jb.core.handlers.zip.archives.BadZipArchive;
import net.zulkar.jb.core.handlers.zip.archives.InitializedZipArchive;

import java.io.IOException;
import java.util.List;

public class LazyZipArchiveFileEntity extends ProxyFileEntity {

    private final ZipHandler zipHandler;
    private volatile ZipArchive archive; //actually this is same as DCL-singleton

    LazyZipArchiveFileEntity(ZipHandler zipHandler, FileEntity archiveFile) {
        super(archiveFile);
        this.zipHandler = zipHandler;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    private ZipArchive getArchive() {
        if (archive != null) {
            return archive;
        }

        synchronized (this) {
            if (archive == null) {
                try {
                    archive = new InitializedZipArchive(entity, zipHandler.readStructure(entity), zipHandler, this);
                } catch (IOException e) {
                    archive = new BadZipArchive(entity, e);
                }
            }
            return archive;
        }
    }


    @Override
    public List<FileEntity> ls() throws IOException {
        return getArchive().ls();
    }
}
