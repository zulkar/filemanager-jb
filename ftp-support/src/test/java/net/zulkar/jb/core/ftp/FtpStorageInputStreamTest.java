package net.zulkar.jb.core.ftp;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class FtpStorageInputStreamTest {

    @Test
    void shouldNotRequestRemotefterGetInputStream() throws Exception {

        TestFtpServer ftpServer = new TestFtpServer().start();
        FtpStorage storage = new FtpStorage(new ZipHandler(), ftpServer.getFtpParameters());

        FileEntity zip = storage.resolve("/somedir/zip.zip");
        try (InputStream ignore = zip.openInputStream()) {
        }
        ;

        ftpServer.close();

        try (InputStream ignore = zip.openInputStream()) {
        }

    }

    @Test
    void shouldNotRequestFtpRemotefterGetInputStream2() throws Exception {

        TestFtpServer ftpServer = new TestFtpServer().start();
        FtpStorage storage = new FtpStorage(new ZipHandler(), ftpServer.getFtpParameters());

        FileEntity zip = storage.resolve("/somedir/zip.zip");
        FileEntity file2 = storage.resolve("/somedir/zip.zip/1/File2.txt");

        try (InputStream ignore = zip.openInputStream()) {
        }

        ftpServer.close();

        try (InputStream ignore = zip.openInputStream()) {
        }
        try (InputStream ignore = file2.openInputStream()) {
        }

    }

    @Test
    void shouldFailIfServerDownGetInputStream() throws Exception {

        TestFtpServer ftpServer = new TestFtpServer().start();
        FtpStorage storage = new FtpStorage(new ZipHandler(), ftpServer.getFtpParameters());

        FileEntity zip = storage.resolve("/somedir/zip.zip");
        ftpServer.close();

        assertThrows(IOException.class, () -> {
            try (InputStream ignore = zip.openInputStream()) {
            }
        });
    }
}
