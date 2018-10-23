package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FileListModelTest {

    @Mock
    private Storage storage;
    @Mock
    private IconLoader iconLoader;
    @Mock
    private FileEntity entity;

    @Mock
    private FileEntity child1;
    @Mock
    private FileEntity child2;

    @Mock
    private FileEntity grandChild11;
    @Mock
    private FileEntity grandChild12;

    private FileListModel model;

    @BeforeEach
    public void mockStorage() throws IOException {
        lenient().when(storage.getRootEntity()).thenReturn(entity);
        lenient().when(entity.ls()).thenReturn(Arrays.asList(child1, child2));
        lenient().when(child1.ls()).thenReturn(Arrays.asList(grandChild11, grandChild12));
        lenient().when(grandChild11.ls()).thenReturn(null);
        lenient().when(grandChild12.ls()).thenReturn(null);
        lenient().when(child2.ls()).thenReturn(Collections.emptyList());

        lenient().when(child1.getParent()).thenReturn(entity);
        lenient().when(child2.getParent()).thenReturn(entity);
        lenient().when(grandChild11.getParent()).thenReturn(child1);

        lenient().when(storage.resolve("/")).thenReturn(entity);
        lenient().when(storage.resolve("/child1")).thenReturn(child1);
        lenient().when(storage.resolve("/child2")).thenReturn(child2);
        lenient().when(storage.resolve("/child1/grandChild11")).thenReturn(grandChild11);
        lenient().when(storage.resolve("/child1/grandChild11")).thenReturn(grandChild12);

        lenient().when(entity.getName()).thenReturn("/");
        lenient().when(child1.getName()).thenReturn("child1");
        lenient().when(child2.getName()).thenReturn("child2");
        lenient().when(grandChild11.getName()).thenReturn("grandChild11");
        lenient().when(grandChild12.getName()).thenReturn("grandChild12");

        model = new FileListModel(iconLoader, storage);

    }

    @Test
    public void shouldShowTwoRowsForNullParent() throws IOException {
        assertEquals(2, model.getRowCount());
    }

    @Test
    public void shouldShowThreeRowsIfHaveParent() throws IOException {
        assertTrue(model.setPath("/child1"));
        assertEquals(3, model.getRowCount());
        assertEquals("..", model.getValueAt(0, 1));
    }

    @Test
    public void shouldNotChangePathIfLsReturnNull() throws IOException {
        assertFalse(model.setPath("/child1/grandChild11"));
    }

    @Test
    public void shouldNotChangePathIfNotResolvable() throws IOException {
        assertFalse(model.setPath("not resolvable"));
    }

    @Test
    public void shouldChangePathIfLsReturnEmpty() throws IOException {
        assertTrue(model.setPath("/child2"));
        assertEquals(1, model.getRowCount());
        assertEquals("..", model.getValueAt(0, 1));
    }

}