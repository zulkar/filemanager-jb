package net.zulkar.jb.core.local;

import net.zulkar.jb.core.ResourcePathFinder;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalStorageIntegrationTest {

    private File resourceDir;
    private String storagetestPath;
    private LocalStorage storage;

    @BeforeEach
    public void before() {
        resourceDir = ResourcePathFinder.getResourceFile("storagetest.zip").getParentFile();
        storagetestPath = LocalFileSystemFactory.getLocalFileSystem().pathToEntityModel(new File(resourceDir, "storagetest.zip").getAbsolutePath());
        storage = new LocalStorage(new ZipHandler(), ResourcePathFinder.getRootDir("storagetest.zip"));
    }


    @Test
    public void shouldResolveContainerFile() throws IOException {
        FileEntity entity = storage.resolve(storagetestPath);
        assertTrue(entity.isContainer());
        assertFalse(entity.isDir());
    }

    @Test
    public void shouldResolveInnerFile() throws IOException {
        FileEntity entity = storage.resolve(storagetestPath + "/1/File2.txt");
        assertFalse(entity.isContainer());
        assertFalse(entity.isDir());
    }

}
