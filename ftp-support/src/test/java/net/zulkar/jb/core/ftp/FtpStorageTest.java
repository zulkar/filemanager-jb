package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.FileEntityTestUtils;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FtpStorageTest {
    private static FakeFtpServer server;
    @Mock
    private ContainerHandler containerHandler;

    @BeforeAll
    public static void initServer() throws InterruptedException {
        server = new FakeFtpServer();
        server.addUserAccount(new UserAccount("user", "password", "/home/user"));
        server.setFileSystem(createFileSystem());
        server.setServerControlPort(0);
        server.start();
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
    public void shouldConnectWithCredentials() throws IOException {
        FtpParameters parameters = givenFtpParameters("user", "password");
        FtpStorage ftpStorage = new FtpStorage(containerHandler, parameters);
    }

    @Test
    public void shouldResolveRootDirFiles() throws IOException {
        FtpParameters parameters = givenFtpParameters("user", "password");
        FtpStorage ftpStorage = new FtpStorage(containerHandler, parameters);
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
    public void shouldResolveTopFileName() throws IOException {
        doTestFileFesolve("/toplevelRootFile.txt", "toplevelRootFile");
    }

    private FileEntity doTestLs(String path, String[] expected) throws IOException {
        FtpParameters parameters = givenFtpParameters("user", "password");
        FtpStorage ftpStorage = new FtpStorage(containerHandler, parameters);
        FileEntity entity = ftpStorage.resolve(path);
        assertTrue(entity.isDir());
        assertEquals(FilenameUtils.normalizeNoEndSeparator(path), entity.getAbsolutePath());
        FileEntityTestUtils.checkFiles(expected, entity.ls());
        return entity;
    }

    private void doTestFileFesolve(String path, String content) throws IOException {
        FtpParameters parameters = givenFtpParameters("user", "password");
        FtpStorage ftpStorage = new FtpStorage(containerHandler, parameters);
        FileEntity entity = ftpStorage.resolve(path);
        assertFalse(entity.isDir());
        assertEquals(path, entity.getAbsolutePath());
        assertEquals(content, IOUtils.toString(entity.openInputStream()));
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
        ftpParameters.setUser(user);
        ftpParameters.setPassword(password);
        ftpParameters.setTimeoutMilliseconds(30000);
        return ftpParameters;
    }

}