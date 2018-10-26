package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.jobs.JobExecutor;
import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.preview.Previewer;
import net.zulkar.jb.core.ui.render.FileListPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
abstract class FileManagerActionTest<T extends FileManagerAction> {
    private FileManagerAction.Factory<T> factory;

    @Mock
    protected UiContext context;

    @Mock
    protected MainFrame mainFrame;

    @Mock
    protected JobExecutor jobExecutor;
    @Mock
    protected ActionEvent event;

    protected T action;


    protected void init(FileManagerAction.Factory<T> factory) {
        action = Mockito.spy(factory.create(context));
        lenient().when(context.getMainFrame()).thenReturn(mainFrame);
        lenient().when(context.getJobExecutor()).thenReturn(jobExecutor);
    }


    @Test
    void shouldRunIfLockAndIgnoreLock() {
        assumeTrue(action.ignoreLock);
        when(context.isActionsLocked()).thenReturn(true);

        action.actionPerformed(event);
        verify(action).doAction(same(event));
    }

    @Test
    void shouldRunIfNotLockAndIgnoreLock() {
        assumeTrue(action.ignoreLock);
        when(context.isActionsLocked()).thenReturn(false);
        action.actionPerformed(event);
        verify(action).doAction(same(event));
    }

    @Test
    void shouldNotRunIfLockAndRespectLock() {
        assumeFalse(action.ignoreLock);
        when(context.isActionsLocked()).thenReturn(true);
        action.actionPerformed(event);
        verify(action, never()).doAction(same(event));
    }

    @Test
    void shouldRunIfNotLockAndRespectLock() {
        assumeFalse(action.ignoreLock);
        when(context.isActionsLocked()).thenReturn(false);
        action.actionPerformed(event);
        verify(action).doAction(same(event));
    }

}
