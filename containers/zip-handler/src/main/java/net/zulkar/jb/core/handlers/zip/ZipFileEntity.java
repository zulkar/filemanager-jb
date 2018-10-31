package net.zulkar.jb.core.handlers.zip;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.handlers.zip.archives.InitializedZipArchive;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipEntry;

public class ZipFileEntity implements FileEntity {

    private final InitializedZipArchive zipArchiveFileEntity;
    private final ZipEntry zipEntry;

    public ZipFileEntity(InitializedZipArchive zipArchiveFileEntity, ZipEntry zipEntry) {
        this.zipArchiveFileEntity = zipArchiveFileEntity;
        this.zipEntry = zipEntry;
    }

    @Override
    public String getAbsolutePath() {
        return zipArchiveFileEntity.getAbsolutePath() + "/" + FilenameUtils.normalizeNoEndSeparator(zipEntry.getName(), true);
    }

    @Override
    public FileEntity getParent() {
        return zipArchiveFileEntity.getParent(zipEntry.getName());
    }

    @Override
    public List<FileEntity> ls() {
        if (!isDir()) {
            return null;
        }
        return zipArchiveFileEntity.ls(this.zipEntry.getName());
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(FilenameUtils.normalizeNoEndSeparator(zipEntry.getName(), true));
    }

    @Override
    public String getExtension() {
        return FilenameUtils.getExtension(zipEntry.getName());
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return zipArchiveFileEntity.openInputStream(this);
    }

    InitializedZipArchive getInitializedZipArchive() {
        return zipArchiveFileEntity;
    }

    ZipEntry getZipEntry() {
        return zipEntry;
    }

    @Override
    public boolean isDir() {
        return zipEntry.isDirectory();
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public long getSize() {
        return zipEntry.getSize();
    }

    @Override
    public Instant getModificationTime() {
        FileTime lastModifiedTime = zipEntry.getLastModifiedTime();
        if (lastModifiedTime != null) {
            return zipEntry.getLastModifiedTime().toInstant();
        }
        return Instant.EPOCH;

    }

    @Override
    public String toString() {
        return "ZipFileEntity{" +
                "zipEntry=" + zipEntry +
                '}';
    }
}
