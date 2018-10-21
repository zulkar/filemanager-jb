package net.zulkar.jb.core.local;

import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalStorage extends AbstractStorage {


    public LocalStorage(ContainerHandler containerHandler, File fileRoot) {
        super(containerHandler, fileRoot.getPath());
    }

    @Override
    public FileEntity resolve(String path) throws IOException {
        path = FilenameUtils.separatorsToUnix(path);
        File file = new File(path);
        if (file.exists()) {
            return wrapIfContainer(new LocalFileEntity(file, this));
        }
        File realFile = findRealFile(file.getCanonicalPath());

        FileEntity entity = new LocalFileEntity(realFile, this);
        return resolveInnerPath(entity, path);
    }


    private File findRealFile(String path) throws FileNotFoundException {
        String[] pathElements = StringUtils.split(path, "/");
        if (pathElements == null || pathElements.length == 0) {
            throw new IllegalArgumentException("cannot resolve null path");
        }
        File file = new File(new File("/"), pathElements[0]);
        int i = 1;


        while (i < pathElements.length) {
            File newFile = new File(file, pathElements[i++]);
            if (!newFile.exists()) {
                if (file.isDirectory()) {
                    throw new FileNotFoundException(path);
                }
                return file;
            }
            file = newFile;
        }
        throw new IllegalStateException("Internal error - should not request findRealFile for existant files");
    }

    @Override
    public void close() throws Exception {
    }


    @Override
    public String toString() {
        return "LocalStorage: " + getName();
    }
}
