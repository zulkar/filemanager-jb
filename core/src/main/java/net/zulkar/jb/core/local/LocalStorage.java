package net.zulkar.jb.core.local;

import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.domain.FileEntity;

import java.io.File;
import java.io.IOException;

public class LocalStorage extends AbstractStorage<LocalFileEntity> {


    private final File fileRoot;

    public LocalStorage(ContainerHandler containerHandler, File fileRoot) {
        super(containerHandler, fileRoot.getPath(), LocalFileSystemFactory.getLocalFileSystem().isCaseSensitive());
        this.fileRoot = fileRoot;
    }

    @Override
    public void close() {
    }

    @Override
    public FileEntity resolve(String path) throws IOException {
        path = LocalFileSystemFactory.getLocalFileSystem().pathToEntityModel(path);
        return validateDirAccess(super.resolve(path));
    }

    @Override
    public String toString() {
        return "LocalStorage: " + getName();
    }

    @Override
    protected LocalFileEntity getFrom(FileEntity current, String pathElement) {
        String realPath = LocalFileSystemFactory.getLocalFileSystem().pathFromEntityModel(current.getAbsolutePath(), fileRoot);
        return fromFile(new File(realPath));
    }

    @Override
    protected LocalFileEntity tryGetNonContainerEntity(String path) {
        File file = new File(fileRoot, path);
        return fromFile(file);
    }

    private LocalFileEntity fromFile(File file) {
        if (file.exists()) {
            return new LocalFileEntity(file, this);
        }
        return null;
    }

    @Override
    public LocalFileEntity getRootEntity() throws IOException {
        return validateDirAccess(new LocalFileEntity(fileRoot, this));
    }

    private <T extends FileEntity> T validateDirAccess(T entity) throws IOException {
        if (entity.isDir() && entity.ls() == null) {
            throw new IOException(String.format("Access denied to %s", entity.getAbsolutePath()));
        }
        return entity;
    }

    @Override
    public boolean needCache() {
        return false;
    }

    @Override
    public String getSystemInternalPath(FileEntity entity) {
        return LocalFileSystemFactory.getLocalFileSystem().pathFromEntityModel(entity.getAbsolutePath(), fileRoot);
    }
}
