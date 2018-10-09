package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class FileEntityTestUtils {
    private FileEntityTestUtils() {
    }

    public static FileEntity find(String fileName, FileEntity[] entities) {
        List<FileEntity> list = Stream.of(entities).filter(f -> f.getName().equals(fileName)).collect(Collectors.toList());
        assertEquals(1, list.size());
        return list.iterator().next();
    }

    public static void checkFiles(String[] expected, FileEntity[] actual) {
        Object[] actualNames = Stream.of(actual).map(FileEntity::getName).sorted().toArray();
        String[] expectedNames = Arrays.copyOf(expected, expected.length);
        Arrays.sort(expectedNames);
        assertArrayEquals(expectedNames, actualNames);
    }
}
