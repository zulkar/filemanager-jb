package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class FileEntityTestUtils {
    private FileEntityTestUtils() {
    }

    public static FileEntity find(String fileName, List<FileEntity> entities) {
        if (entities == null){
            return null;
        }
        List<FileEntity> list = entities.stream().filter(f -> f.getName().equals(fileName)).collect(Collectors.toList());
        assertEquals(1, list.size());
        return list.iterator().next();
    }

    public static void checkFiles(String[] expected, List<FileEntity> actual) {
        Object[] actualNames = actual.stream().map(FileEntity::getName).sorted().toArray();
        String[] expectedNames = Arrays.copyOf(expected, expected.length);
        Arrays.sort(expectedNames);
        assertArrayEquals(expectedNames, actualNames);
    }
}
