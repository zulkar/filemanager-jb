package net.zulkar.jb.core.ui.storage;

import net.zulkar.jb.core.domain.Storage;

import java.io.IOException;

@FunctionalInterface
public interface StorageSupplier {
    Storage get() throws IOException;
}
