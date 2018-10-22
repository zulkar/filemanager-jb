package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ftp.FtpParameters;
import net.zulkar.jb.core.ftp.FtpStorage;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import net.zulkar.jb.core.local.LocalStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StorageManager implements AutoCloseable {

    private static final Logger log = LogManager.getLogger(StorageManager.class);
    private final List<Storage> storages;
    private final ContainerHandler handler;
    private final Map<FtpParameters, FtpStorage> storageMap;


    public StorageManager() {
        storages = new ArrayList<>();
        handler = new ZipHandler();
        Arrays.stream(File.listRoots()).map(lr -> new LocalStorage(handler, lr)).forEach(storages::add);
        storageMap = new HashMap<>();
    }

    public Storage[] getAllAvailableStorages() {
        return Stream.concat(storages.stream(), storageMap.values().stream()).collect(Collectors.toList()).toArray(new Storage[0]);
    }

    public Storage createFtpStorage(FtpParameters parameters) {
        try {
            FtpStorage ftpStorage = storageMap.get(parameters);
            if (ftpStorage == null) {
                ftpStorage = new FtpStorage(handler, parameters);
                storageMap.put(parameters, ftpStorage);
            }
            return ftpStorage;
        } catch (Exception e) {
            log.error(e);
            return null;
        }

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
