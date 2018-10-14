package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.AbstractStorage;
import net.zulkar.jb.core.ContainerHandler;
import net.zulkar.jb.core.domain.FileEntity;

import java.io.IOException;

public class FtpStorage extends AbstractStorage {

    private final String host;
    private final String user;
    private final String password;

    public FtpStorage(ContainerHandler containerHandler, String host, String user, String password) {
        super(containerHandler);
        this.host = host;
        this.user = user;
        this.password = password;

    }

    @Override
    public FileEntity resolve(String path) throws IOException {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
