package net.zulkar.jb.core.handlers.zip;

import net.zulkar.jb.core.FileEntityTestUtils;
import net.zulkar.jb.core.ResourcePathFinder;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.local.LocalFileEntity;
import net.zulkar.jb.core.local.LocalStorage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZipHandlerTest {

    private File resourceZipFile;
    private File rootFile;
    private FileEntity resourceZipFileEntity;
    private ZipHandler zipHandler = new ZipHandler();

    @BeforeEach
    public void before() {
        resourceZipFile = ResourcePathFinder.getResourceFile("resources.zip");
        rootFile = ResourcePathFinder.getRootDir("resources.zip");
        resourceZipFileEntity = new LocalFileEntity(resourceZipFile, mock(LocalStorage.class));
    }

    @Test
    public void shouldReadListOfInnerZipDir() throws IOException {
        FileEntity dir1 = resolve("Dir1");
        assertEquals("Dir1", dir1.getName());
        assertTrue(dir1.isDir());
        assertFileNameEquals(resourceZipFile.getAbsolutePath() + "/Dir1", dir1.getAbsolutePath());
        FileEntityTestUtils.checkFiles(new String[]{"Zdir3", "empty.zip", "FileA.txt", "FileB"}, dir1.ls());
    }

    @Test
    public void shouldReadListOfTopLevelZipDir() throws IOException {
        FileEntity zipEntity = zipHandler.createFrom(resourceZipFileEntity);
        FileEntityTestUtils.checkFiles(new String[]{"Dir1", "FileA", "test-no-entry.zip"}, zipEntity.ls());
    }

    @Test
    public void shouldThrowAnExceptionWhenReadDir() throws IOException {
        FileEntity dir1 = resolve("Dir1");
        assertTrue(dir1.isDir());

        assertThrows(FileNotFoundException.class, () -> {
            dir1.openInputStream();
        });
    }


    @Test
    public void shouldReadFileData() throws IOException {
        FileEntity file = resolve("Dir1/FileA.txt");
        file.getName();
        assertEquals("FileA.txt", file.getName());
        assertEquals("txt", file.getExtension());
        assertFalse(file.isDir());
        assertFileNameEquals(resourceZipFile.getAbsolutePath() + "/Dir1/FileA.txt", file.getAbsolutePath());
        assertNull(file.ls());
        assertEquals("Dir1/FileA content\n", IOUtils.toString(file.openInputStream()));
    }

    @Test
    public void shouldReadTwoFileContentInRowData() throws IOException {
        List<FileEntity> files = resolve("Dir1").ls();
        FileEntity fileA = FileEntityTestUtils.find("FileA.txt", files);
        FileEntity fileB = FileEntityTestUtils.find("FileB", files);

        assertEquals("Dir1/FileA content\n", IOUtils.toString(fileA.openInputStream()));
        assertEquals("Dir1/FileB content\n", IOUtils.toString(fileB.openInputStream()));
    }

    @Test
    public void shouldReadEmptyArchive() throws IOException {
        FileEntity fileEntry = zipHandler.createFrom(new LocalFileEntity(ResourcePathFinder.getResourceFile("Dir1/empty.zip"), mock(LocalStorage.class)));

        assertTrue(fileEntry.isContainer());
        assertNotNull(fileEntry.ls());
        assertTrue(fileEntry.ls().isEmpty());
    }

    @Test
    public void shouldReadNestedArchive() throws IOException {
        FileEntity zipEntity = resolve("Dir1/Zdir3/Zdir3.zip");
        FileEntity zipInsideZip = zipHandler.createFrom(zipEntity);
        FileEntityTestUtils.checkFiles(new String[]{"zdir1", "FileC.txt"}, zipInsideZip.ls());
        FileEntity fileC = FileEntityTestUtils.find("FileC.txt", zipInsideZip.ls());
        assertEquals("FileC content\n", IOUtils.toString(fileC.openInputStream()));
    }

    @Test
    public void shouldNotReadFileDataEager() throws IOException {
        FileEntity entity = mock(FileEntity.class);
        zipHandler.createFrom(entity);
        verify(entity, never()).openInputStream();
    }

    @Test
    public void shouldReadWinrarFileWithIncompleteEntrySet() throws IOException {
        File noEntryFile = ResourcePathFinder.getResourceFile("test-no-entry.zip");
        FileEntity zipEntity = zipHandler.createFrom(new LocalFileEntity(noEntryFile, mock(LocalStorage.class)));
        FileEntity dir = FileEntityTestUtils.find("parent", zipEntity.ls());
        FileEntity file = FileEntityTestUtils.find("test.txt", dir.ls());
        Assertions.assertNotNull(file);
    }

    @Test
    public void shouldReadArchiveOnlyOnce() throws IOException {
        FileEntity localEntityZipSpy = Mockito.spy(new LocalFileEntity(resourceZipFile, mock(LocalStorage.class)));
        FileEntity archiveEntity = zipHandler.createFrom(localEntityZipSpy);
        archiveEntity.ls();
        archiveEntity.ls();
        verify(localEntityZipSpy, times(1)).openInputStream();
    }

    @Test
    public void shouldReturnParent() throws IOException {
        FileEntity entity = resolve("Dir1/Zdir3/");
        assertEquals("Dir1", entity.getParent().getName());
    }

    @Test
    public void shouldReturnSameParentForTopLevel() throws IOException {
        FileEntity zipEntity = zipHandler.createFrom(resourceZipFileEntity);
        FileEntity dir1 = FileEntityTestUtils.find("Dir1", zipEntity.ls());
        assertSame(zipEntity, dir1.getParent());
    }

    private void assertFileNameEquals(String expectedLocalFileName, String actual) {
        String expectedEntityName = "/" + FilenameUtils.normalizeNoEndSeparator(StringUtils.removeStart(expectedLocalFileName, rootFile.getAbsolutePath()), true);
        assertEquals(expectedEntityName, actual);
    }


    private FileEntity resolve(String innerPath) throws IOException {
        FileEntity entity = zipHandler.createFrom(resourceZipFileEntity);
        String[] pathElements = StringUtils.split(innerPath, "/");
        for (String pathElement : pathElements) {
            entity = FileEntityTestUtils.find(pathElement, entity.ls());
        }
        return entity;
    }

}