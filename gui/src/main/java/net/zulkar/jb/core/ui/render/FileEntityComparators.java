package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.domain.FileEntity;

import java.util.Comparator;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public final class FileEntityComparators {
    private FileEntityComparators() {
    }

    public static Comparator<FileEntity> dirFirst() {
        return Comparator.comparing(FileEntity::isDir, Boolean::compare).reversed();
    }

    public static Comparator<FileEntity> nameIgnoreCase() {
        return Comparator.comparing(FileEntity::getName, CASE_INSENSITIVE_ORDER);
    }
}
