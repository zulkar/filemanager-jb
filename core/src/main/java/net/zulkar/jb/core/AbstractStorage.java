package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public abstract class AbstractStorage implements Storage {

    private final ContainerHandler containerHandler;

    public AbstractStorage(ContainerHandler containerHandler) {
        this.containerHandler = containerHandler;
    }

    protected FileEntity resolveInnerPath(FileEntity entity, String fullPath) throws IOException {
        String internalPath = StringUtils.removeStart(fullPath, entity.getAbsolutePath());
        String[] pathElements = StringUtils.split(internalPath, "/");

        for (String pathElement : pathElements) {
            entity = wrapIfContainer(entity);
            FileEntity nextEntity = findChild(pathElement, entity.ls());
            if (nextEntity == null) {
                throw new FileNotFoundException(fullPath);
            }
            entity = wrapIfContainer(nextEntity);
        }
        return entity;
    }

    public FileEntity wrapIfContainer(FileEntity entity) {
        if (containerHandler.maySupport(entity)) {
            entity = containerHandler.createFrom(entity);
        }
        return entity;
    }

    protected FileEntity findChild(String pathElement, List<FileEntity> children) {
        return children.stream().filter(c -> c.getName().equals(pathElement)).findFirst().orElse(null);
    }
}
