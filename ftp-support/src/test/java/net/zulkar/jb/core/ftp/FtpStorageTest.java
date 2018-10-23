package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.FileEntityTestUtils;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FtpStorageTest {
    private static FakeFtpServer server;
    @Mock
    private ContainerHandler containerHandler;

    private FtpStorage ftpStorage;

    @BeforeAll
    public static void initServer() throws InterruptedException, IOException {
        server = new FakeFtpServer();
        server.addUserAccount(new UserAccount("user", "password", "/home/user"));
        server.setFileSystem(createFileSystem());
        server.setServerControlPort(0);
        server.start();
        System.out.println("starting server at " + server.getServerControlPort());
        for (File cacheDirectory : FtpStorage.getCacheDirectories()) {
            FileUtils.deleteDirectory(cacheDirectory);
        }

    }

    private static FileSystem createFileSystem() {
        UnixFakeFileSystem system = new UnixFakeFileSystem();
        system.setCreateParentDirectoriesAutomatically(true);
        system.add(new FileEntry("/home/user/dir1/file1.txt", "some data file1"));
        system.add(new FileEntry("/home/user/dir1/file2.txt", "some data file2"));
        system.add(new FileEntry("/home/user/toplevelUserFile.txt", "toplevelUserFile"));
        system.add(new FileEntry("/toplevelRootFile.txt", "toplevelRootFile"));
        system.add(new DirectoryEntry("/home/user/emptyDir"));
        return system;
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }


    @Test
    public void shouldResolveRootDirFiles() throws IOException {
        assertNotNull(ftpStorage.resolve("/"));
        assertNotNull(ftpStorage.resolve(""));
    }

    @Test
    public void shouldLsRootDirFiles() throws IOException {
        FileEntity entity = doTestLs("/", new String[]{"home", "toplevelRootFile.txt"});
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
        FtpParameters parameters = givenFtpParameters("user", "bad password");
        assertThrows(IOException.class, () -> new FtpStorage(containerHandler, parameters));
    }

    private FtpParameters givenFtpParameters(String user, String password) {
        FtpParameters ftpParameters = new FtpParameters();
        ftpParameters.setHost("localhost");
        ftpParameters.setPort(server.getServerControlPort());
        //ftpParameters.setPort(2121);
        ftpParameters.setUser(user);
        //ftpParameters.setUser("anonymous");
        ftpParameters.setPassword(password);
        //ftpParameters.setPassword("");
        return ftpParameters;
    }

    @BeforeEach
    public void createFtpStorage() throws IOException {
        FtpParameters parameters = givenFtpParameters("user", "password");
        ftpStorage = new FtpStorage(containerHandler, parameters);
    }

    @AfterEach
    public void deleteFtpStorage() throws Exception {
        ftpStorage.close();
    }


}