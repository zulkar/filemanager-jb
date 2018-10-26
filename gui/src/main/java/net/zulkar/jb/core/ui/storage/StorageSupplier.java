package net.zulkar.jb.core.ui.storage;

import net.zulkar.jb.core.cache.CacheableStorage;

import java.io.IOException;

@FunctionalInterface
public interface StorageSupplier {
    CacheableStorage get() throws IOException;
}
