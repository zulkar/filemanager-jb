package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static java.util.Collections.emptyList;
import static net.zulkar.jb.core.FileEntityTestUtils.checkFiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({ResourcePathExtension.class})
class LocalStorageTest {

    private final File rootDir;

    LocalStorageTest(File rootDir) {
        this.rootDir = rootDir;
    }

    @Test
    public void shouldShowFileListForRoot() throws IOException {
        LocalStorage localStorage = givenLocalStorage();
        FileEntity[] fileEntities = localStorage.listFiles(new FileEntity("/"));
        checkFiles(new String[]{"storage", "core.resource.txt"}, fileEntities);
    }

    @Test
    public void shouldNotShowFilesAboveRoot() throws IOException {
        LocalStorage localStorage = givenLocalStorage();
        FileEntity[] fileEntities = localStorage.listFiles(new FileEntity("/../.."));
        checkFiles(new String[]{"storage", "core.resource.txt"}, fileEntities);
    }

    @Test
    public void shouldShowFileListForString() throws IOException {
        LocalStorage localStorage = givenLocalStorage();
        FileEntity[] fileEntities = localStorage.listFiles(new FileEntity("/storage/dir1/dir11"));
        checkFiles(new String[]{"dir11_1", "File1", "File2"}, fileEntities);
    }

    @Test
    public void shouldShowFileListForStringEndingWithSeparator() throws IOException {
        LocalStorage localStorage = givenLocalStorage();
        FileEntity[] fileEntities = localStorage.listFiles(new FileEntity("/storage/dir1/dir11/"));
        checkFiles(new String[]{"dir11_1", "File1", "File2"}, fileEntities);
    }

    @Test
    public void shouldShowFileListForResolvedDir() throws IOException {
        LocalStorage localStorage = givenLocalStorage();
        FileEntity[] fileEntities = localStorage.listFiles(new FileEntity("storage/dir1/../.."));
        checkFiles(new String[]{"storage", "core.resource.txt"}, fileEntities);
    }

    @Test
    public void shouldReadInputStream() throws IOException {
        LocalStorage localStorage = givenLocalStorage();
        try (InputStream is = localStorage.openInputStream(new FileEntity("/storage/dir1/file1.txt"))) {
            assertEquals("test data", IOUtils.toString(is));
        }
    }

    @Test
    public void shouldReadFileListFromContainer() throws IOException {
        ContainerHandler containerHandler = mock(ContainerHandler.class);
        when(containerHandler.maySupport(any(), any())).thenReturn(Boolean.TRUE);
        FileEntity[] expected = new FileEntity[]{new FileEntity("ContainerFile1"), new FileEntity("ContainerFile2")};
        when(containerHandler.listFiles(any(), any(), any())).thenReturn(expected);
        LocalStorage localStorage = givenLocalStorageWithDummyContainerHandler(containerHandler);
        FileEntity[] actual = localStorage.listFiles(new FileEntity("storage/dir1/file1.txt"));
        checkFiles(new String[]{"ContainerFile1", "ContainerFile2"}, actual);
    }

    @Test
    public void shouldReadFileListFromContainerLevel2() throws IOException {
        ContainerHandler containerHandler = mock(ContainerHandler.class);
        when(containerHandler.maySupport(any(), any())).thenReturn(Boolean.TRUE);
        FileEntity[] expected = new FileEntity[]{new FileEntity("ContainerFile1"), new FileEntity("ContainerFile2")};
        when(containerHandler.listFiles(any(), any(), any())).thenReturn(expected);
        LocalStorage localStorage = givenLocalStorageWithDummyContainerHandler(containerHandler);
        FileEntity[] actual = localStorage.listFiles(new FileEntity("storage/dir1/file1.txt/ContainerFile1"));
        checkFiles(new String[]{"ContainerFile1", "ContainerFile2"}, actual);
    }

    @Test
    public void shouldRedirectRequestToContainer() throws IOException {
        ContainerHandler containerHandler = mock(ContainerHandler.class);
        when(containerHandler.maySupport(any(), any())).thenReturn(Boolean.TRUE);
        LocalStorage localStorage = givenLocalStorageWithDummyContainerHandler(containerHandler);
        FileEntity[] actual = localStorage.listFiles(new FileEntity("storage/dir1/file1.txt/ContainerFile1"));
        verify(containerHandler).listFiles(eq(localStorage), any(), eq(new FileEntity("storage/dir1/file1.txt/ContainerFile1")));
    }

    private LocalStorage givenLocalStorage() {
        return new LocalStorage(rootDir, emptyList());
    }

    private LocalStorage givenLocalStorageWithDummyContainerHandler(ContainerHandler containerHandler) {
        return new LocalStorage(rootDir, Collections.singletonList(containerHandler));
    }

}