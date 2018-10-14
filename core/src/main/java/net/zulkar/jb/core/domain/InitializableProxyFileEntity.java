package net.zulkar.jb.core.domain;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

public abstract class InitializableProxyFileEntity implements FileEntity {
    protected FileEntity entity;

    protected InitializableProxyFileEntity(FileEntity entity) {
        this(entity, false);
    }

    protected InitializableProxyFileEntity(FileEntity entity, boolean initialized) {
        this.entity = entity;
    }

    protected abstract void init();

    @Override
    public String getAbsolutePath() {
        return entity.getAbsolutePath();
    }


    @Override
    public FileEntity getParent() {
        return entity.getParent();
    }

    @Override
    public List<FileEntity> ls() {
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
    public LocalDateTime getModificationTime() {
        return entity.getModificationTime();
    }
}
