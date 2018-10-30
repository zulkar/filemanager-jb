package net.zulkar.jb.core.handlers.zip;

import com.google.common.io.ByteStreams;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.SystemUtils;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipHandler implements ContainerHandler {
    private static final long MAX_FILE_SIZE;
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("jar", "zip", "war", "ear");

    static {
        MAX_FILE_SIZE = SystemUtils.getLongProperty("net.zulkar.jb.zip.maxfilesize", 200 * 1024 * 1024);//200 MB
    }

    @Override
    public boolean maySupport(FileEntity file) {
        return SUPPORTED_EXTENSIONS.contains(StringUtils.lowerCase(file.getExtension()));
    }

    @Override
    public FileEntity createFrom(FileEntity entity) {
        if (!maySupport(entity)) {
            return entity;
        }
        if (entity instanceof LazyZipArchiveFileEntity) {
            return entity;
        }

        return new LazyZipArchiveFileEntity(this, entity);
    }


    InputStream readContent(ZipFileEntity entity) throws IOException {
        if (entity.isDir()) {
            throw new FileNotFoundException(String.format("File entity %s is a directory", entity.getName()));
        }
        ZipInputStream zis = new ZipInputStream(entity.getZipArchiveFileEntity().openInputStream());
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            if (entry.getName().equals(entity.getZipEntry().getName())) {
                break;
            }
            entry = zis.getNextEntry();
        }
        if (entry == null) {
            throw new IOException("cannot open entity" + entity);
        }
        return withZipBombProtection(zis);

    }

    private InputStream withZipBombProtection(ZipInputStream zis) throws IOException {
        if (MAX_FILE_SIZE == -1) {
            return zis;
        }
        return ByteStreams.limit(zis, MAX_FILE_SIZE);
    }

    void init(LazyZipArchiveFileEntity archiveFileEntity, FileEntity realEntity) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(realEntity.openInputStream())) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                archiveFileEntity.add(e);
            }
            archiveFileEntity.checkAllNodesHaveEntries();
        }

    }
}
