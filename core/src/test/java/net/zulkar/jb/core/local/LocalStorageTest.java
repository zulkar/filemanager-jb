package net.zulkar.jb.core.local;

import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.ResourcePathFinder;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class LocalStorageTest {

    @Mock
    private ContainerHandler containerHandler;
    private File resourceDir;
    private String resourcePath;
    private LocalStorage localStorage;

    @BeforeEach
    public void before() throws IOException {
        resourceDir = ResourcePathFinder.getResourceFile("core.resource.txt").getParentFile();
        resourcePath = FilenameUtils.normalizeNoEndSeparator(resourceDir.getCanonicalPath(), true);
        localStorage = new LocalStorage(containerHandler, new File("/"));
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


    private void doTestResolveFile(String path) throws IOException {
        String absolutePath = resourcePath + path;
        assertEquals(FilenameUtils.separatorsToUnix(new File(absolutePath).getCanonicalPath()),
                localStorage.resolve(absolutePath).getAbsolutePath());
    }
}