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
import java.util.function.BiPredicate;
import java.util.function.Function;

public abstract class AbstractStorage<FE extends FileEntity> implements Storage {
    private static final Logger log = LogManager.getLogger(AbstractStorage.class);

    protected final ContainerHandler containerHandler;
    private String name;
    private BiPredicate<FileEntity, String> unknownChildrenSearchPredicate;

    protected AbstractStorage(ContainerHandler containerHandler, String name, boolean caseSensitive) {
        this.containerHandler = containerHandler;
        this.name = name;
        this.unknownChildrenSearchPredicate = getSearchPredicate(caseSensitive);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FileEntity resolve(String path) throws IOException {
        path = FilenameUtils.normalizeNoEndSeparator(path, true);

        FileEntity entity = resolveRealEntity(path);
        String internalPath = StringUtils.removeStart(path, entity.getAbsolutePath());
        if (StringUtils.isEmpty(internalPath)) {
            return wrapIfContainer(entity);
        }
        log.debug("Resolving {} as {} and {}", path, entity.getAbsolutePath(), internalPath);
        return resolveInnerPath(entity, path, internalPath);
    }

    protected FileEntity resolveRealEntity(String path) throws IOException {
        FE file = tryGetNonContainerEntity(path);
        if (file != null) {
            log.debug("{}: Resolving path {} to real fileEntity", name, path);
            return file;
        }
        log.debug("{}: Resolving path {} using file tree descending", name, path);
        return resolveRealFilePathDescending(path);


    }


    protected abstract FE getFrom(FileEntity current, String pathElement) throws IOException;

    protected abstract FE tryGetNonContainerEntity(String path) throws IOException;

    @Override
    public abstract FE getRootEntity() throws IOException;

    protected FileEntity resolveInnerPath(FileEntity entity, String fullPath, String internalPath) throws IOException {
        return descend(fullPath, internalPath, entity,
                (current, pathElement) -> findChildForUnknownEntities(pathElement, current.ls()),
                (current) -> {
                    log.error("Cannot resolve {} inside {}", internalPath, current);
                    throw new FileNotFoundException(fullPath);
                },
                this::wrapIfContainer);
    }

    private FileEntity resolveRealFilePathDescending(String path) throws IOException {
        return descend(path, path, getRootEntity(),
                this::getFrom,
                current -> {
                    if (current.isDir()) {
                        throw new FileNotFoundException(path);
                    }
                    return current;
                },
                Function.identity()
        );
    }


    private FileEntity descend(String pathForLogs,
                               String pathToDescend,
                               FileEntity startFrom,
                               NextFunction next,
                               NextIsNull nextIsNull,
                               Function<FileEntity, FileEntity> map) throws IOException {
        String[] pathElements = StringUtils.split(pathToDescend, "/");
        if (pathElements == null || pathElements.length == 0) {
            return startFrom;
        }
        FileEntity current = map.apply(startFrom);
        for (String pathElement : pathElements) {
            FileEntity nextEntity = next.getNext(current, pathElement);
            if (nextEntity == null) {
                return nextIsNull.getCurrent(current);
            }
            current = map.apply(nextEntity);
        }
        return current;
    }


    public FileEntity wrapIfContainer(FileEntity entity) {
        if (containerHandler.maySupport(entity)) {
            return containerHandler.createFrom(entity);
        }
        return entity;
    }

    private FileEntity findChildForUnknownEntities(String pathElement, List<FileEntity> children) {
        if (children == null) {
            return null;
        }
        return children.stream().filter(Objects::nonNull).filter(c -> unknownChildrenSearchPredicate.test(c, pathElement)).findFirst().orElse(null);
    }

    private BiPredicate<FileEntity, String> getSearchPredicate(boolean caseSensitive) {
        if (caseSensitive) {
            return (entity, pathElement) -> entity.getName().equals(pathElement);
        } else {
            return (entity, pathElement) -> entity.getName().equalsIgnoreCase(pathElement);
        }

    }


    @FunctionalInterface
    public interface NextFunction {
        FileEntity getNext(FileEntity e, String next) throws IOException;
    }

    @FunctionalInterface
    public interface NextIsNull {
        FileEntity getCurrent(FileEntity current) throws FileNotFoundException;
    }
}
