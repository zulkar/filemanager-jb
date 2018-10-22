package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FtpStorage extends AbstractStorage<FtpRemoteEntity> {
    private static final Logger log = LogManager.getLogger(FtpStorage.class);
    private final static String PREFIX = "net.zulkar.jb-ftp";
    private final FTPClient ftpClient;
    private final FtpParameters ftpParameters;
    private final File cacheDir;

    public FtpStorage(ContainerHandler containerHandler, FtpParameters ftpParameters) throws IOException {
        super(containerHandler, String.format("%s@%s:%d", ftpParameters.getUser(), ftpParameters.getHost(), ftpParameters.getPort()));
        this.ftpParameters = ftpParameters;
        ftpClient = new FTPClient();
        connect();
        cacheDir = Files.createTempDirectory(PREFIX).toFile();
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
        log.info("Logged into to {}", ftpParameters.getHost());
    }


    @Override
    protected FtpRemoteEntity getFrom(FileEntity current, String pathElement) throws IOException {
        FTPFile ftpFile = find(ftpClient.listFiles(current.getAbsolutePath()), pathElement);
        if (ftpFile == null) {
            return null;
        }
        return new FtpRemoteEntity(ftpFile, this, FilenameUtils.normalizeNoEndSeparator(current.getAbsolutePath()) + "/" + pathElement);
    }

    @Override
    protected FtpRemoteEntity tryGetRealEntity(String path) throws IOException {
        String parent = FilenameUtils.getFullPathNoEndSeparator(path);
        FTPFile ftpFile = find(ftpClient.listFiles(parent), FilenameUtils.getName(path));
        if (ftpFile == null) {
            return null;
        }
        return new FtpRemoteEntity(ftpFile, this, FilenameUtils.normalizeNoEndSeparator(path));
    }

    @Override
    protected FtpRemoteEntity getRootEntity() {
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

    private FTPFile find(FTPFile[] ftpFiles, String pathElement) {
        if (ftpFiles == null || ftpFiles.length == 0) {
            return null;
        }
        return Arrays.stream(ftpFiles).filter(f -> f.getName().equals(pathElement)).findFirst().orElse(null);
    }


    @Override
    public void close() throws Exception {
        ftpClient.disconnect();
        FileUtils.deleteDirectory(cacheDir);
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
        File cachedFile = new File(cacheDir, fullPath);
        if (cachedFile.exists()) {
            return new FileInputStream(cachedFile);
        } else {
            if (cachedFile.getParentFile().mkdirs()) {
                try (FileOutputStream fos = new FileOutputStream(cachedFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    cacheInto(bos, fullPath);
                }
                return new FileInputStream(cachedFile);

            } else { //try to read into memory
                log.error("Cannot cache file {} into {}", fullPath, cachedFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                cacheInto(bos, fullPath);
                return new ByteArrayInputStream(bos.toByteArray());
            }

        }

    }

    private void cacheInto(OutputStream os, String fullPath) throws IOException {
        try (InputStream is = ftpClient.retrieveFileStream(fullPath)) {
            IOUtils.copy(is, os);
            if (!ftpClient.completePendingCommand()) {
                throw new IOException(String.format("File trasfer %s failed", fullPath));
            }
        }
    }

    @Override
    public String toString() {
        return "FtpStorage: " + getName();
    }

    public static File[] getCacheDirectories() {
        return new File(System.getProperty("java.io.tmpdir")).listFiles((d, n) -> n.startsWith(PREFIX));

    }
}
