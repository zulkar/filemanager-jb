package net.zulkar.jb.core.cache;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.ProxyFileEntity;

import java.io.IOException;
import java.io.InputStream;

public class CachedDataFileEntity extends ProxyFileEntity {

    private final CacheableStorage cacheableStorage;

    protected CachedDataFileEntity(FileEntity entity, CacheableStorage cacheableStorage) {
        super(entity);
        this.cacheableStorage = cacheableStorage;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return cacheableStorage.cachedData(entity);
    }
}
