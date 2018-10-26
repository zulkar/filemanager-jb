package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.FileEntityTestUtils;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Tag("IntegrationTest")
class FtpStorageTest {

    private ContainerHandler containerHandler = new ZipHandler();

    private FtpStorage ftpStorage;

    private static TestFtpServer testFtpServer;

    @BeforeAll
    public static void initServer() throws InterruptedException, IOException {
        testFtpServer = new TestFtpServer().start();
    }


    @AfterAll
    public static void stopServer() {
        testFtpServer.stop();
    }


    @Test
    public void shouldResolveRootDirFiles() throws IOException {
        assertNotNull(ftpStorage.resolve("/"));
        assertNotNull(ftpStorage.resolve(""));
    }

    @Test
    public void shouldLsRootDirFiles() throws IOException {
        FileEntity entity = doTestLs("/", new String[]{"home", "toplevelRootFile.txt", "somedir"});
        assertNull(entity.getParent());
    }


    @Test
    public void shouldLsHomeFiles() throws IOException {
        FileEntity entity = doTestLs("/home/user", new String[]{"dir1", "emptyDir", "toplevelUserFile.txt"});
        assertNotNull(entity.getParent());
    }

    @Test
    public void shouldLsEndSeparator() throws IOException {
        doTestLs("/home/user/", new String[]{"dir1", "emptyDir", "toplevelUserFile.txt"});
    }

    @Test
    public void shouldLsWindowsDirFiles() throws IOException {
        doTestLs("\\home\\user", new String[]{"dir1", "emptyDir", "toplevelUserFile.txt"});
    }

    @Test
    public void shouldResolveUserFileName() throws IOException {
        doTestFileFesolve("/home/user/toplevelUserFile.txt", "toplevelUserFile");
    }

    @Test
    public void shouldResolveFileNameTwice() throws IOException {
        doTestFileFesolve("/toplevelRootFile.txt", "toplevelRootFile");
        doTestFileFesolve("/toplevelRootFile.txt", "toplevelRootFile");
    }

    @Test
    public void shouldResolveParent() throws IOException {
        FileEntity entity = doTestFileFesolve("/home/user/toplevelUserFile.txt", "toplevelUserFile");
        doTestFileFesolve("/home/user/toplevelUserFile.txt", "toplevelUserFile");
        FileEntity parent = entity.getParent();
        assertTrue(parent.isDir());
        assertEquals("/home/user", parent.getAbsolutePath());
    }

    @Test
    public void shouldReadZipArchiveParent() throws IOException {
        doTestFileFesolve("/somedir/zip.zip/1/File2.txt", "File2 data \n");
    }

    @Test
    public void shouldResolveZip() throws IOException {
        FileEntity entity = ftpStorage.resolve("/somedir/zip.zip");
        assertTrue(entity.isContainer());
    }


    private FileEntity doTestLs(String path, String[] expected) throws IOException {

        FileEntity entity = ftpStorage.resolve(path);
        assertTrue(entity.isDir());
        assertEquals(FilenameUtils.normalizeNoEndSeparator(path, true), entity.getAbsolutePath());
        FileEntityTestUtils.checkFiles(expected, entity.ls());
        return entity;
    }

    private FileEntity doTestFileFesolve(String path, String content) throws IOException {

        FileEntity entity = ftpStorage.resolve(path);
        assertFalse(entity.isDir());
        assertEquals(path, entity.getAbsolutePath());
        try (InputStream is = entity.openInputStream()) {
            assertEquals(content, IOUtils.toString(is));
        }

        return entity;
    }

    @Test
    public void shouldNotConnectWithBadCredentials() throws IOException {
        FtpParameters parameters = testFtpServer.getFtpParameters();
        parameters.setUser("wronguser");
        assertThrows(IOException.class, () -> new FtpStorage(containerHandler, parameters));
    }


    @BeforeEach
    public void createFtpStorage() throws IOException {
        FtpParameters parameters = testFtpServer.getFtpParameters();
        ftpStorage = new FtpStorage(containerHandler, parameters);
    }

    @AfterEach
    public void deleteFtpStorage() throws Exception {
        ftpStorage.close();
    }


}