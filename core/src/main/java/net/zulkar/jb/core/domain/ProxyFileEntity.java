package net.zulkar.jb.core.domain;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

public abstract class ProxyFileEntity implements FileEntity {
    protected FileEntity entity;


    protected ProxyFileEntity(FileEntity entity) {
        this.entity = entity;
    }

    @Override
    public String getAbsolutePath() {
        return entity.getAbsolutePath();
    }


    @Override
    public FileEntity getParent() throws IOException {
        return entity.getParent();
    }

    @Override
    public List<FileEntity> ls() throws IOException {
        return entity.ls();
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public String getExtension() {
        return entity.getExtension();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return entity.openInputStream();
    }

    @Override
    public boolean isDir() {
        return entity.isDir();
    }

    @Override
    public boolean isContainer() {
        return entity.isContainer();
    }

    @Override
    public long getSize() {
        return entity.getSize();
    }

    @Override
    public Instant getModificationTime() {
        return entity.getModificationTime();
    }
}
