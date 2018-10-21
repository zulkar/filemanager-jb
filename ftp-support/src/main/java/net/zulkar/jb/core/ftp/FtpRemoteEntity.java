package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

public class FtpRemoteEntity implements FileEntity {
    private final FtpStorage ftpStorage;
    private String fullPath;
    private final FTPFile file;

    public FtpRemoteEntity(FTPFile file, FtpStorage ftpStorage, String fullPath) {
        this.file = file;
        this.ftpStorage = ftpStorage;
        this.fullPath = fullPath;
    }

    @Override
    public String getAbsolutePath() {
        return fullPath;
    }

    @Override
    public FileEntity getParent() throws IOException {
        return ftpStorage.resolve(FilenameUtils.getFullPath(getAbsolutePath()));
    }

    @Override
    public List<FileEntity> ls() throws IOException {
        return ftpStorage.ls(this);
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(file.getName());
    }

    @Override
    public String getExtension() {
        return FilenameUtils.getExtension(file.getName());
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return ftpStorage.getInputStream(fullPath);
    }

    @Override
    public boolean isDir() {
        return file.isDirectory();
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public long getSize() {
        return file.getSize();
    }

    @Override
    public Instant getModificationTime() {
        if (file == null || file.getTimestamp() == null) {
            return Instant.ofEpochMilli(0);
        }
        return file.getTimestamp().toInstant();
    }
}
