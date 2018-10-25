package net.zulkar.jb.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.zulkar.jb.core.SystemUtils;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CacheableStorage implements Storage {
    private final static String PREFIX = "net.zulkar.jb-cache";
    private final File cacheDir;
    private static final Logger log = LogManager.getLogger(CacheableStorage.class);

    public final Storage storage;

    public final LoadingCache<String, Optional<FileEntity>> cache;
    private final long maxSize;

    public CacheableStorage(Storage storage) throws IOException {
        this.storage = storage;
        this.maxSize = SystemUtils.getLongProperty("net.zulkar.jb.cachesize", 10000L);
        if (storage.needCache()) {
            cache = CacheBuilder.newBuilder()
                    .maximumSize(maxSize)
                    .build(new EntityCacheLoader());
            cacheDir = Files.createTempDirectory(PREFIX).toFile();
        } else {
            cache = null;
            cacheDir = null;
        }

    }

    @Override
    public FileEntity resolve(String path) throws IOException {
        if (!storage.needCache()) {
            return storage.resolve(path);
        }
        try {
            log.debug("retrieving path {}", path);
            return cache.get(path).orElse(null);

        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw new IOException(e.getCause()); // throwing new exception greatly helps in issue resolving
            } else {
                throw new RuntimeException(e.getCause());
            }
        }
    }


    @SuppressWarnings("OptionalAssignedToNull")
    public synchronized void invalidate(String path) throws IOException {
        if (!storage.needCache()) {
            return;
        }
        if (cache.getIfPresent(path) != null) {
            cache.invalidateAll();
            FileUtils.cleanDirectory(cacheDir);
            // We cannot invalidate cache only this entity, because it can be an archive entry.
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
        if (children == null) {
            try (InputStream ignored = cachedData(resolved)) {
            }
        } else {
            if (children.size() > maxSize - 2) {
                log.warn("Entity {} has {} children, cannot cache them all", path, children.size());
            }
            for (FileEntity child : children) {
                resolve(child.getAbsolutePath());
            }
        }
        return resolved;
    }

    synchronized FileInputStream cachedData(FileEntity entity) throws IOException { //todo: remove syncronized, should lock on file
        File cachedFile = new File(cacheDir, entity.getAbsolutePath());
        if (!cachedFile.exists()) {
            if (tryToCreateParentFileIfNotExists(cachedFile)) {
                log.debug("{}: entity {} to be cached in {}", this, entity, cachedFile);
                try (InputStream is = entity.openInputStream();
                     FileOutputStream fos = new FileOutputStream(cachedFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    IOUtils.copy(is, bos);
                }
            }
        }
        return new FileInputStream(cachedFile);
    }


    private boolean tryToCreateParentFileIfNotExists(File cachedFile) {
        return cachedFile.getParentFile().exists() || cachedFile.getParentFile().mkdirs();
    }


    public static File[] getCacheDirectories() {
        return new File(System.getProperty("java.io.tmpdir")).listFiles((d, n) -> n.startsWith(PREFIX));
    }

    private class EntityCacheLoader extends CacheLoader<String, Optional<FileEntity>> {

        @Override
        public Optional<FileEntity> load(String path) throws IOException {
            log.debug("Path {} was not found in cache - trying to resolve", path);
            return Optional.ofNullable(storage.resolve(path)).map(e -> new CachedDataFileEntity(e, CacheableStorage.this));
        }

    }
}
