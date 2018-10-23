package net.zulkar.jb.core.ui.preview;

import net.zulkar.jb.core.domain.FileEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnionPreviewerTest {

    @Mock
    private Previewer previewer1;

    @Mock
    private Previewer previewer2;

    private FileEntity fileEntity;

    @Test
    public void shouldDispatchToPreviewerOnlyOnce() throws IOException {
        UnionPreviewer previewer = new UnionPreviewer(previewer1, previewer2);

        when(previewer1.supports(fileEntity)).thenReturn(true);

        assertTrue(previewer.supports(fileEntity));

        previewer.preview(fileEntity);

        verify(previewer1).preview(fileEntity);
        verify(previewer2, never()).preview(fileEntity);
    }

}