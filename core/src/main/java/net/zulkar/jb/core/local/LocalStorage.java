package net.zulkar.jb.core.local;

import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.domain.FileEntity;

import java.io.File;

public class LocalStorage extends AbstractStorage<LocalFileEntity> {


    private final File fileRoot;

    public LocalStorage(ContainerHandler containerHandler, File fileRoot) {
        super(containerHandler, fileRoot.getPath());
        this.fileRoot = fileRoot;
    }

    @Override
    public void close() {
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
    public LocalFileEntity getRootEntity() {
        return new LocalFileEntity(fileRoot, this);
    }

    @Override
    public boolean needCache() {
        return false;
    }
}
