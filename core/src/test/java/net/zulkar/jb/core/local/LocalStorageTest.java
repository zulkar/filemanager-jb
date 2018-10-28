package net.zulkar.jb.core.local;

import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.ResourcePathFinder;
import net.zulkar.jb.core.domain.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.*;


@ExtendWith(MockitoExtension.class)
class LocalStorageTest {

    @Mock
    private ContainerHandler containerHandler;
    private File resourceDir;
    private File rootDir;
    private String resourcePath;
    private LocalStorage localStorage;

    @BeforeEach
    public void before() throws IOException {
        resourceDir = ResourcePathFinder.getResourceFile("core.resource.txt").getParentFile();
        resourcePath = LocalFileSystemFactory.getLocalFileSystem().pathToEntityModel(resourceDir.getCanonicalPath());
        localStorage = new LocalStorage(containerHandler, ResourcePathFinder.getRootDir("core.resource.txt"));
    }

    @Test
    public void shouldResolveExistingFile() throws IOException {
        doTestResolveFile("/storage/dir1/file1.txt");
    }

    @Test
    public void shouldResolveExistingDir() throws IOException {
        doTestResolveFile("/storage/dir1");
    }

    @Test
    public void shouldResolveExistingDirEndSeparator() throws IOException {
        doTestResolveFile("/storage/dir1/");
    }

    @Test
    public void shouldNotResolveUnexistantFile() throws IOException {
        assertThrows(FileNotFoundException.class, () -> doTestResolveFile("/storage/dir1/NonExistantFile"));
    }

    @Test
    @EnabledOnOs({LINUX, MAC, SOLARIS})
    public void absolutePathShouldBeSameAsInternalPathForUnix() throws IOException {
        FileEntity fileEntity = doTestResolveFile("/storage/dir1/file1.txt");
        assertEquals(fileEntity.getAbsolutePath(), localStorage.getSystemInternalPath(fileEntity));
    }

    @Test
    @EnabledOnOs({WINDOWS})
    public void absolutePathShouldDiffer() throws IOException {
        LocalStorage diskC = new LocalStorage(containerHandler, new File("c:\\"));
        FileEntity windows = diskC.resolve("c:\\Windows");
        assertEquals("/Windows", windows.getAbsolutePath());
        assertEquals("c:\\Windows", diskC.getSystemInternalPath(windows));
    }

    private FileEntity doTestResolveFile(String path) throws IOException {
        String absolutePath = resourcePath + path;
        FileEntity resolved = localStorage.resolve(absolutePath);
        assertEquals(LocalFileSystemFactory.getLocalFileSystem().pathToEntityModel(new File(absolutePath).getCanonicalPath()),
                resolved.getAbsolutePath());
        return resolved;
    }
}