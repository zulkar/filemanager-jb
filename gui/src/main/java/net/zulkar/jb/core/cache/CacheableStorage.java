package net.zulkar.jb.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.zulkar.jb.core.SystemUtils;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

//todo: to be refactored
public class CacheableStorage implements Storage {
    private final static String PREFIX = "net.zulkar.jb-entityCache";
    private final File cacheDir;
    private static final Logger log = LogManager.getLogger(CacheableStorage.class);
    private final FileEntity rootEntity;

    public final Storage storage;

    public final LoadingCache<String, Optional<FileEntity>> entityCache;
    public final LoadingCache<String, Optional<List<FileEntity>>> childrenCache;

    private final long maxSize;

    public CacheableStorage(Storage storage) throws IOException {
        this.storage = storage;
        this.maxSize = SystemUtils.getLongProperty("net.zulkar.jb.cachesize", 10000L);
        rootEntity = storage.getRootEntity();
        if (storage.needCache()) {
            entityCache = CacheBuilder.newBuilder()
                    .maximumSize(maxSize)
                    .build(new EntityCacheLoader());
            childrenCache = CacheBuilder.newBuilder()
                    .maximumSize(maxSize)
                    .build(new ChildrenCacheLoader());
            cacheDir = Files.createTempDirectory(PREFIX).toFile();
        } else {
            entityCache = null;
            cacheDir = null;
            childrenCache = null;
        }

    }

    @Override
    public FileEntity resolve(String path) throws IOException {
        if (!storage.needCache()) {
            return storage.resolve(path);
        }
        try {
            log.debug("retrieving path {}", path);
            return entityCache.get(path).orElse(null);

        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw new IOException(e.getCause()); // throwing new exception greatly helps in issue resolving
            } else {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    public List<FileEntity> ls(FileEntity entity) throws IOException {
        if (!storage.needCache()) {
            return entity.ls();
        }
        try {
            return childrenCache.get(entity.getAbsolutePath()).orElse(null);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw new IOException(e.getCause()); // throwing new exception greatly helps in issue resolving
            } else {
                throw new RuntimeException(e.getCause());
            }
        }

    }

    public FileEntity getParent(FileEntity entity) throws IOException {
        if (!storage.needCache()) {
            return entity.getParent();
        }
        return storage.resolve(FilenameUtils.normalizeNoEndSeparator(FilenameUtils.getFullPath(entity.getAbsolutePath()), true));
    }


    @SuppressWarnings("OptionalAssignedToNull")
    public synchronized void invalidate(String path) throws IOException {
        if (!storage.needCache()) {
            return;
        }
        if (entityCache.getIfPresent(path) != null) {
            entityCache.invalidateAll();
            FileUtils.cleanDirectory(cacheDir);
            // We cannot invalidate entityCache only this entity, because it can be an archive entry.
            // todo: need to found if entity is an archive, and invalidate only entities from this archive
        }
    }

    @Override
    public String getName() {
        return storage.getName();
    }

    @Override
    public FileEntity getRootEntity() {
        return storage.getRootEntity();
    }

    @Override
    public boolean needCache() {
        return false;
    }

    @Override
    public void close() throws Exception {
        storage.close();
        if (storage.needCache()) {
            FileUtils.deleteDirectory(cacheDir);
        }
    }

    public FileEntity ensureCached(String path) throws IOException {
        if (!storage.needCache()) {
            return storage.resolve(path);
        }

        FileEntity resolved = this.resolve(path);
        if (resolved == null) {
            return null;
        }
        if (resolved.getParent() != null) {
            this.resolve(resolved.getParent().getAbsolutePath());
        }

        if (resolved.getParent() != null) {
            this.resolve(resolved.getParent().getAbsolutePath());
        }
        List<FileEntity> children = resolved.ls();
        if (children != null) {
            if (children.size() > maxSize - 2) {
                log.warn("Entity {} has {} children, cannot entityCache them all", path, children.size());
            }
            for (FileEntity child : children) {
                resolve(child.getAbsolutePath());
            }
        }
        return resolved;
    }

    public void ensureDataCached(FileEntity entity) throws IOException {
        try (InputStream ignored = entity.openInputStream()) {
        }
    }


    private class EntityCacheLoader extends CacheLoader<String, Optional<FileEntity>> {

        @Override
        public Optional<FileEntity> load(String path) throws IOException {
            log.debug("Path {} was not found in entityCache - trying to resolve", path);
            return Optional.ofNullable(storage.resolve(path));
        }

    }

    private class ChildrenCacheLoader extends CacheLoader<String, Optional<List<FileEntity>>> {

        @Override
        public Optional<List<FileEntity>> load(String path) throws IOException {
            log.debug("Path {} was not found in chidlren cache - trying to resolve", path);
            ;
            return Optional.ofNullable(CacheableStorage.this.resolve(path)).map(e -> {
                try {
                    return e.ls();
                } catch (IOException e1) {
                    throw new UncheckedIOException(e1);
                }
            });
        }

    }
}
