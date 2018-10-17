package net.zulkar.jb.core.ftp;

import com.google.common.io.Files;
import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FtpStorage extends AbstractStorage {

    private final FTPClient ftpClient;
    private final FtpParameters ftpParameters;
    private final File cacheDir;

    public FtpStorage(ContainerHandler containerHandler, FtpParameters ftpParameters) throws IOException {
        super(containerHandler);
        this.ftpParameters = ftpParameters;
        ftpClient = new FTPClient();
        connect();
        cacheDir = Files.createTempDir();

    }

    private void connect() throws IOException {
        FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpClient.configure(conf);
        ftpClient.connect(ftpParameters.getHost(), ftpParameters.getPort());
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            throw new IOException(String.format("Connect to %s failed with code: %d", ftpParameters.getHost(), replyCode));
        }
        if (!ftpClient.login(ftpParameters.getUser(), ftpParameters.getPassword())) {
            throw new IOException(String.format("Cannot connect into %s as %s", ftpParameters.getHost(), ftpParameters.getUser()));
        }
    }

    @Override
    public synchronized FileEntity resolve(String path) throws IOException {
        path = FilenameUtils.normalizeNoEndSeparator(path);
        FtpRemoteEntity entity = findEntity(path);
        return resolveInnerPath(entity, path);
    }


    private FtpRemoteEntity findEntity(String path) throws IOException {
        String[] pathElements = StringUtils.split(path, "/");
        if (pathElements == null) {
            throw new IllegalArgumentException("cannot resolve null path");
        }
        if (pathElements.length == 0) {
            FTPFile root = new FTPFile();
            root.setType(FTPFile.DIRECTORY_TYPE);
            root.setSize(-1);
            root.setName("/");
            return new FtpRemoteEntity(root, this, "/");
        }


        FTPFile file = find(ftpClient.listFiles("/"), pathElements[0]);
        if (file == null) {
            throw new FileNotFoundException(path);
        }

        int i = 1;
        String currentPath = "/" + FilenameUtils.getBaseName(pathElements[0]);

        while (i < pathElements.length) {
            if (file.isDirectory()) {
                FTPFile[] ftpFiles = ftpClient.listFiles(currentPath);
                file = find(ftpFiles, pathElements[i]);
                if (file == null) {
                    break;
                }
                currentPath = FilenameUtils.normalizeNoEndSeparator(FilenameUtils.concat(currentPath, pathElements[i]), true);
                i++;
            } else {
                return new FtpRemoteEntity(file, this, currentPath);
            }
        }
        if (file == null) {
            throw new FileNotFoundException(path);
        }
        return new FtpRemoteEntity(file, this, path);
    }

    private FTPFile find(FTPFile[] ftpFiles, String pathElement) {
        return Arrays.stream(ftpFiles).filter(f -> f.getName().equals(pathElement)).findFirst().orElse(null);
    }


    @Override
    public void close() throws Exception {
        ftpClient.disconnect();
    }

    synchronized List<FileEntity> ls(FtpRemoteEntity entity) throws IOException {
        if (entity.isDir()) {
            return Arrays.stream(ftpClient.listFiles(entity.getAbsolutePath())).map(f ->
                    new FtpRemoteEntity(f,
                            this,
                            FilenameUtils.normalizeNoEndSeparator(
                                    FilenameUtils.concat(entity.getAbsolutePath(), f.getName()), true)))
                    .collect(Collectors.toList());
        }
        return null;
    }

    synchronized InputStream getInputStream(String fullPath) throws IOException {
        return ftpClient.retrieveFileStream(fullPath);
    }
}
