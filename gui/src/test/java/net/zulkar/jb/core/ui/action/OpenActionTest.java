package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.jobs.ChangeDirJob;
import net.zulkar.jb.core.jobs.OpenFileJob;
import net.zulkar.jb.core.ui.preview.Previewer;
import net.zulkar.jb.core.ui.render.FileListPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenActionTest extends FileManagerActionTest<OpenAction> {

    @Mock
    protected Previewer previewer;
    @Mock
    protected FileListPanel activePanel;

    @Mock
    protected FileEntity currentEntity;

    @BeforeEach
    void before() {
        super.init(new OpenAction.Factory());
        lenient().when(context.getPreviewer()).thenReturn(previewer);
        lenient().when(mainFrame.getActivePanel()).thenReturn(activePanel);
        lenient().when(activePanel.getCurrentEntity()).thenReturn(currentEntity);
    }

    @Test
    void shouldCallOpenIfDir() {
        when(currentEntity.isDir()).thenReturn(true);
        action.actionPerformed(event);
        verify(jobExecutor).execute(any(ChangeDirJob.class));
    }

    @Test
    void shouldCallOpenIfContainer() {
        when(currentEntity.isContainer()).thenReturn(true);
        action.actionPerformed(event);
        verify(jobExecutor).execute(any(ChangeDirJob.class));
    }

    @Test
    void shouldCallPreviewIfPreviewerSupports() {
        when(previewer.supports(currentEntity)).thenReturn(true);
        action.actionPerformed(event);
        verify(jobExecutor).execute(any(OpenFileJob.class));
    }

    @Test
    void shouldSetStatusElse() {
        when(currentEntity.getName()).thenReturn("Name");
        action.actionPerformed(event);
        verify(mainFrame).setStatus("Cannot open Name");
    }

}