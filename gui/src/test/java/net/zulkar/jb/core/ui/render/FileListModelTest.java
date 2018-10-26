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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
        lenient().when(entity.ls()).thenReturn(Arrays.asList(child1, child2));
        lenient().when(child1.ls()).thenReturn(Arrays.asList(grandChild11, grandChild12));
        lenient().when(grandChild11.ls()).thenReturn(null);
        lenient().when(grandChild12.ls()).thenReturn(null);
        lenient().when(child2.ls()).thenReturn(Collections.emptyList());

        lenient().when(child1.getParent()).thenReturn(entity);
        lenient().when(child2.getParent()).thenReturn(entity);
        lenient().when(grandChild11.getParent()).thenReturn(child1);

        lenient().when(entity.getName()).thenReturn("/");
        lenient().when(child1.getName()).thenReturn("child1");
        lenient().when(child2.getName()).thenReturn("child2");
        lenient().when(grandChild11.getName()).thenReturn("grandChild11");
        lenient().when(grandChild12.getName()).thenReturn("grandChild12");

        model = new FileListModel(iconLoader, new FileListModel.EntityData(entity, null, entity.ls()));

    }

    @Test
    public void shouldShowTwoRowsForNullParent() throws IOException {
        cd(entity);
        assertEquals(2, model.getRowCount());
    }

    @Test
    public void shouldShowThreeRowsIfHaveParent() throws IOException {
        cd(child1);
        assertEquals(3, model.getRowCount());
        assertEquals("..", model.getValueAt(0, 1));
    }


    @Test
    public void shouldShowPathIfLsReturnEmpty() throws IOException {
        cd(child2);
        assertEquals(1, model.getRowCount());
        assertEquals("..", model.getValueAt(0, 1));
    }

    @Test
    void shouldNotRequestAnyDataFromStorageOrEntityRelationsAfterJobComplete() throws IOException {
        cd(child1);
        reset(child1, child2, grandChild11, grandChild12, entity);
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int c = 0; c < model.getColumnCount(); c++) {
                model.getValueAt(r, c);
            }
        }
        model.getCurrent();
        verifyNoRelationsRequested(child1);
        verifyNoRelationsRequested(child2);
        verifyNoRelationsRequested(entity);
        verifyNoRelationsRequested(grandChild11);
        verifyNoRelationsRequested(grandChild12);
    }

    private void verifyNoRelationsRequested(FileEntity entity) throws IOException {
        verify(entity, never()).ls();
        verify(entity, never()).getParent();
    }

    private void cd(FileEntity entity) throws IOException {
        model.setCurrentEntity(new FileListModel.EntityData(entity, entity.getParent(), entity.ls()));

    }

}