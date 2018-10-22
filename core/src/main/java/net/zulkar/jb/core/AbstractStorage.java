package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public abstract class AbstractStorage<FE extends FileEntity> implements Storage {
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

    @Override
    public FileEntity resolve(String path) throws IOException {
        path = FilenameUtils.normalizeNoEndSeparator(path, true);

        FE entity = resolveRealEntity(path);
        String internalPath = StringUtils.removeStart(path, entity.getAbsolutePath());
        if (StringUtils.isEmpty(internalPath)) {
            return wrapIfContainer(entity);
        }
        log.debug("Resolving {} as {} and {}", path, entity.getAbsolutePath(), internalPath);
        return resolveInnerPath(entity, path, internalPath);
    }

    protected FE resolveRealEntity(String path) throws IOException {
        FE file = tryGetRealEntity(path);
        if (file != null) {
            log.debug("{}: Resolving path {} to real fileEntity", name, path);
            return file;
        }
        log.debug("{}: Resolving path {} using file tree descending", name, path);
        return resolveRealFilePathDescending(path);


    }

    private FE resolveRealFilePathDescending(String path) throws IOException {
        String[] pathElements = StringUtils.split(path, "/");
        if (pathElements == null || pathElements.length == 0) {
            return getRootEntity();
        }
        FE current = getRootEntity();
        for (String pathElement : pathElements) {
            FE next = getFrom(current, pathElement);
            if (next == null) {
                if (current.isDir()) {
                    throw new FileNotFoundException(path);
                }
                return current;
            }
            current = next;
        }
        throw new IllegalStateException(String.format("descending to bottom %s : %s", path, current)); // should not happen
    }

    protected abstract FE getFrom(FE current, String pathElement) throws IOException;

    protected abstract FE tryGetRealEntity(String path) throws IOException;

    protected abstract FE getRootEntity();

    protected FileEntity resolveInnerPath(FE entity, String fullPath, String internalPath) throws IOException {
        String[] pathElements = StringUtils.split(internalPath, "/");
        log.debug("resolving inner entity {} inside {} ", internalPath, entity.getAbsolutePath());
        FileEntity current = wrapIfContainer(entity);
        for (String pathElement : pathElements) {
            FileEntity nextEntity = findChild(pathElement, current.ls());
            if (nextEntity == null) {
                log.error("Cannot resolve {} inside {}", pathElement, current);
                throw new FileNotFoundException(fullPath);
            }
            current = wrapIfContainer(nextEntity);
        }
        return current;
    }
    
    public FileEntity wrapIfContainer(FileEntity entity) {
        if (containerHandler.maySupport(entity)) {
            return containerHandler.createFrom(entity);
        }
        return entity;
    }

    protected FileEntity findChild(String pathElement, List<FileEntity> children) {
        if (children == null) {
            return null;
        }
        return children.stream().filter(Objects::nonNull).filter(c -> c.getName().equals(pathElement)).findFirst().orElse(null);
    }

}
