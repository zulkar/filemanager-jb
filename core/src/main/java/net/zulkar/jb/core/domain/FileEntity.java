package net.zulkar.jb.core.domain;


import org.apache.commons.io.FilenameUtils;

import java.util.Objects;

/**
 * file entity, located either on local disk, zip or remote dir
 */
public class FileEntity {
    private final String path;


    public FileEntity(String path) {
        this.path = path;
    }

    public FileEntity(FileEntity parent, String name) {
        this(FilenameUtils.concat(parent.path, name));
    }

    public String getPath() {
        return path;
    }

    public FileEntity getParent() {
        return new FileEntity(FilenameUtils.getFullPath(path));
    }

    public String getName() {
        return FilenameUtils.getName(path);
    }


    public String getExtension() {
        return FilenameUtils.getExtension(path);
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEntity entity = (FileEntity) o;
        return Objects.equals(path, entity.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
