package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FtpStorage extends AbstractStorage<FtpRemoteEntity> {
    private static final Logger log = LogManager.getLogger(FtpStorage.class);

    private final FTPClient ftpClient;
    private final FtpParameters ftpParameters;


    public FtpStorage(ContainerHandler containerHandler, FtpParameters ftpParameters) throws IOException {
        super(containerHandler, String.format("%s@%s:%d", ftpParameters.getUser(), ftpParameters.getHost(), ftpParameters.getPort()));
        this.ftpParameters = ftpParameters;
        ftpClient = new FTPClient();
        connect();

    }

    private void connect() throws IOException {
        FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpClient.configure(conf);
        ftpClient.connect(ftpParameters.getHost(), ftpParameters.getPort());
        log.info("Connected to {}", ftpParameters.getHost());
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            throw new IOException(String.format("Connect to %s failed with code: %d", ftpParameters.getHost(), replyCode));
        }
        ftpClient.enterLocalPassiveMode();
        if (!ftpClient.login(ftpParameters.getUser(), ftpParameters.getPassword())) {
            throw new IOException(String.format("Cannot connect into %s as %s", ftpParameters.getHost(), ftpParameters.getUser()));
        }
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        log.info("Logged into to {}", ftpParameters.getHost());
    }


    @Override
    protected FtpRemoteEntity getFrom(FileEntity current, String pathElement) throws IOException {
        FTPFile ftpFile = find(ftpClient.listFiles(current.getAbsolutePath()), pathElement);
        if (ftpFile == null) {
            return null;
        }
        return new FtpRemoteEntity(ftpFile, this, FilenameUtils.normalizeNoEndSeparator(FilenameUtils.concat(current.getAbsolutePath(), pathElement), true));
    }

    @Override
    protected FtpRemoteEntity tryGetNonContainerEntity(String path) throws IOException {
        String parent = FilenameUtils.getFullPathNoEndSeparator(path);
        FTPFile ftpFile = find(ftpClient.listFiles(parent), FilenameUtils.getName(path));
        if (ftpFile == null) {
            return null;
        }
        return new FtpRemoteEntity(ftpFile, this, FilenameUtils.normalizeNoEndSeparator(path, true));
    }

    @Override
    public FtpRemoteEntity getRootEntity() {
        FTPFile root = new FTPFile();
        root.setType(FTPFile.DIRECTORY_TYPE);
        root.setSize(-1);
        root.setName("/");
        return new FtpRemoteEntity(root, this, "/") {
            @Override
            public FileEntity getParent() throws IOException {
                return null;
            }
        };
    }

    @Override
    public boolean needCache() {
        return true;
    }

    @Override
    public void close() throws Exception {
        ftpClient.disconnect();

    }

    private FTPFile find(FTPFile[] ftpFiles, String pathElement) {
        if (ftpFiles == null || ftpFiles.length == 0) {
            return null;
        }
        return Arrays.stream(ftpFiles).filter(f -> f.getName().equals(pathElement)).findFirst().orElse(null);
    }

    synchronized List<FileEntity> ls(FtpRemoteEntity entity) throws IOException {
        if (entity.isDir()) {
            return Arrays.stream(ftpClient.listFiles(entity.getAbsolutePath())).map(f ->
                    new FtpRemoteEntity(f,
                            this,
                            FilenameUtils.normalizeNoEndSeparator(
                                    FilenameUtils.concat(entity.getAbsolutePath(), f.getName()), true)))
                    .map(this::wrapIfContainer)
                    .collect(Collectors.toList());
        }
        return null;
    }

    synchronized InputStream getInputStream(String fullPath) throws IOException {
        InputStream inputStream = ftpClient.retrieveFileStream(fullPath);

        return new InputStreamProxy(inputStream) {
            @Override
            public void close() throws IOException {
                inputStream.close();
                ftpClient.completePendingCommand();
            }
        };

    }

    @Override
    public String toString() {
        return "FtpStorage: " + getName();
    }


}
