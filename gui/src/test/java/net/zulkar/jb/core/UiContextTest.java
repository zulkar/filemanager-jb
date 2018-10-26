package net.zulkar.jb.core;

import net.zulkar.jb.core.jobs.CancellableBackgroundJob;
import net.zulkar.jb.core.ui.MainFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UiContextTest {

    @Mock
    private MainFrame mainFrame;

    @Mock
    private StorageManager storageManager;


    private UiContext context;

    @BeforeEach
    void before() {
        context = new UiContext(mainFrame, storageManager);
    }

    @Test
    public void shouldLock() {
        context.lockActions(mock(CancellableBackgroundJob.class));
        assertTrue(context.isActionsLocked());
    }

    @Test
    public void shouldUnLock() {
        CancellableBackgroundJob job = mock(CancellableBackgroundJob.class);
        context.lockActions(job);
        context.unlockActions(job);
        assertFalse(context.isActionsLocked());
    }

    @Test
    public void shouldNotUnLockIfNotAll() {
        CancellableBackgroundJob job = mock(CancellableBackgroundJob.class);
        context.lockActions(job);
        context.lockActions(mock(CancellableBackgroundJob.class));
        context.unlockActions(mock(CancellableBackgroundJob.class));
        assertTrue(context.isActionsLocked());
    }

    @Test
    public void shouldNotLockIfStopped() {
        CancellableBackgroundJob job = mock(CancellableBackgroundJob.class);
        context.lockActions(job);
        context.lockActions(mock(CancellableBackgroundJob.class));
        context.stopAllAndUnlock();
        assertFalse(context.isActionsLocked());
    }

}