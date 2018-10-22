package net.zulkar.jb.core.local;


import com.google.common.annotations.VisibleForTesting;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * file entity, located either on local disk, zip or remote dir
 */
public class LocalFileEntity implements FileEntity {


    private File file;
    private LocalStorage storage;

    @VisibleForTesting
    public LocalFileEntity(File file, LocalStorage storage) {
        this.file = file;
        this.storage = storage;
    }

    @Override
    public String getAbsolutePath() {
        return FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath(), true);
    }

    @Override
    public FileEntity getParent() {
        File parent = file.getParentFile();
        return parent == null ? null : new LocalFileEntity(file.getParentFile(), storage);

    }

    @Override
    public List<FileEntity> ls() {
        File[] children = file.listFiles();
        if (children == null) {
            return null;
        }
        return Arrays.stream(children).map(f -> new LocalFileEntity(f, storage)).map(lfe -> storage.wrapIfContainer(lfe)).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return file.getName();
    }


    @Override
    public String getExtension() {
        return FilenameUtils.getExtension(file.getName());
    }


    @Override
    public InputStream openInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    @Override
    public String toString() {
        return file.toString();
    }

    @Override
    public boolean isDir() {
        return file.isDirectory();
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public Instant getModificationTime() {
        return Instant.ofEpochMilli(file.lastModified());
    }

    public File getLocalFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalFileEntity entity = (LocalFileEntity) o;
        return Objects.equals(file, entity.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
