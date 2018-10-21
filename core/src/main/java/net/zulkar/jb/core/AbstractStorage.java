package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public abstract class AbstractStorage implements Storage {
    private static final Logger log = LogManager.getLogger(AbstractStorage.class);

    protected final ContainerHandler containerHandler;
    private String name;

    protected AbstractStorage(ContainerHandler containerHandler, String name) {
        this.containerHandler = containerHandler;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    protected FileEntity resolveInnerPath(FileEntity entity, String fullPath) throws IOException {
        String internalPath = StringUtils.removeStart(fullPath, entity.getAbsolutePath());
        String[] pathElements = StringUtils.split(internalPath, "/");

        log.debug("resolving inner entity {} inside {} ", internalPath, entity.getAbsolutePath());
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
