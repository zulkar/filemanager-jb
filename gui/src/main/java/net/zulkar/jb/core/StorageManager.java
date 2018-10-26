package net.zulkar.jb.core;

import net.zulkar.jb.core.cache.CacheableStorage;
import net.zulkar.jb.core.ftp.FtpParameters;
import net.zulkar.jb.core.ftp.FtpStorage;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import net.zulkar.jb.core.local.LocalStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StorageManager implements AutoCloseable {

    private static final Logger log = LogManager.getLogger(StorageManager.class);
    private final List<CacheableStorage> storages;
    private final ContainerHandler handler;
    private final Map<FtpParameters, CacheableStorage> storageMap;


    public StorageManager() {
        storages = new ArrayList<>();
        handler = new ZipHandler();
        Arrays.stream(File.listRoots()).map(lr -> {
            try {
                return new CacheableStorage(new LocalStorage(handler, lr));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).forEach(storages::add);
        storageMap = new HashMap<>();
    }

    public CacheableStorage[] getAllAvailableStorages() {
        return Stream.concat(storages.stream(), storageMap.values().stream()).collect(Collectors.toList()).toArray(new CacheableStorage[0]);
    }

    public CacheableStorage createFtpStorage(FtpParameters parameters) throws IOException {

        CacheableStorage ftpStorage = storageMap.get(parameters);
        if (ftpStorage == null) {
            ftpStorage = new CacheableStorage(new FtpStorage(handler, parameters));
            storageMap.put(parameters, ftpStorage);
        }
        return ftpStorage;


    }

    @Override
    public void close() throws Exception {
        storageMap.values().forEach(f -> {
            try {
                f.close();
            } catch (Exception e) {
                log.error("Error closing {} ", f, e);
            }
        });
    }
}
