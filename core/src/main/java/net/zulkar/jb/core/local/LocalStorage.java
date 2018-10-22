package net.zulkar.jb.core.local;

import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import org.apache.commons.lang3.Validate;

import java.io.File;

public class LocalStorage extends AbstractStorage<LocalFileEntity> {


    private final File fileRoot;

    public LocalStorage(ContainerHandler containerHandler, File fileRoot) {
        super(containerHandler, fileRoot.getPath());
        Validate.isTrue(fileRoot.exists());
        Validate.isTrue(fileRoot.isDirectory());
        this.fileRoot = fileRoot;
    }

    @Override
    public void close() throws Exception {
    }


    @Override
    public String toString() {
        return "LocalStorage: " + getName();
    }

    @Override
    protected LocalFileEntity getFrom(LocalFileEntity current, String pathElement) {
        return tryGetRealEntity(new File(current.getLocalFile(), pathElement).getAbsolutePath());
    }

    @Override
    protected LocalFileEntity tryGetRealEntity(String path) {
        File file = new File(path);
        if (file.exists()) {
            return new LocalFileEntity(file, this);
        }
        return null;
    }

    @Override
    protected LocalFileEntity getRootEntity() {
        return new LocalFileEntity(fileRoot, this);
    }
}
