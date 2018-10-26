package net.zulkar.jb.core.ftp;

import org.apache.commons.io.IOUtils;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.IOException;

public class TestFtpServer implements AutoCloseable {

    private static FakeFtpServer server;


    public TestFtpServer() throws IOException {
        server = new FakeFtpServer();
        server.addUserAccount(new UserAccount("user", "password", "/home/user"));
        server.setFileSystem(createFileSystem());
        server.setServerControlPort(0);
    }


    public TestFtpServer start() {
        server.start();
        System.out.println("Server started at " + server.getServerControlPort());
        return this;
    }


    public int getPort() {
        return server.getServerControlPort();
    }

    public void stop() {
        server.stop();
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    private FileSystem createFileSystem() throws IOException {
        UnixFakeFileSystem system = new UnixFakeFileSystem();
        system.setCreateParentDirectoriesAutomatically(true);
        system.add(new FileEntry("/home/user/dir1/file1.txt", "some data file1"));
        system.add(new FileEntry("/home/user/dir1/file2.txt", "some data file2"));
        system.add(new FileEntry("/home/user/toplevelUserFile.txt", "toplevelUserFile"));
        system.add(new FileEntry("/toplevelRootFile.txt", "toplevelRootFile"));
        FileEntry zipAcrhive = new FileEntry("/somedir/zip.zip");
        zipAcrhive.setContents(IOUtils.toByteArray(FtpStorageTest.class.getClassLoader().getResourceAsStream("zip.zip")));
        system.add(zipAcrhive);
        system.add(new DirectoryEntry("/home/user/emptyDir"));
        return system;
    }

    public FtpParameters getFtpParameters() {
        FtpParameters ftpParameters = new FtpParameters();
        ftpParameters.setHost("localhost");
        ftpParameters.setPort(getPort());
        ftpParameters.setUser("user");
        ftpParameters.setPassword("password");
        return ftpParameters;
    }


}
