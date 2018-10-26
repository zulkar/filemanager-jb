package net.zulkar.jb.core;

import com.google.common.collect.Sets;
import net.zulkar.jb.core.jobs.CancellableBackgroundJob;
import net.zulkar.jb.core.jobs.JobExecutor;
import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.preview.ImageViewer;
import net.zulkar.jb.core.ui.preview.Previewer;
import net.zulkar.jb.core.ui.preview.TextPreviewer;
import net.zulkar.jb.core.ui.preview.UnionPreviewer;
import net.zulkar.jb.core.ui.storage.ChooseStorageDialog;

import javax.annotation.concurrent.GuardedBy;
import java.awt.*;
import java.util.Set;

public class UiContext {

    private final MainFrame mainFrame;
    private final Previewer previewer;
    private final StorageManager storageManager;
    private final JobExecutor jobExecutor;

    @GuardedBy("this")
    private final Set<CancellableBackgroundJob<?>> lockSets = Sets.newIdentityHashSet();

    public UiContext(MainFrame mainFrame, StorageManager storageManager) {
        this.mainFrame = mainFrame;
        previewer = new UnionPreviewer(new ImageViewer(mainFrame), new TextPreviewer(mainFrame));
        this.storageManager = storageManager;
        this.jobExecutor = new JobExecutor();
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public Previewer getPreviewer() {
        return previewer;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }


    /**
     * run from EDT only
     */
    public void unlockActions(CancellableBackgroundJob<?> job) {
        lockSets.remove(job);
        if (!isActionsLocked()) {
            this.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * run from EDT only
     */
    public void lockActions(CancellableBackgroundJob<?> job) {
        this.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lockSets.add(job);
    }

    /**
     * run from EDT only
     */
    public boolean isActionsLocked() {
        return !lockSets.isEmpty();
    }

    public void stopAllAndUnlock() {
        while (!lockSets.isEmpty()) {
            CancellableBackgroundJob<?> job = lockSets.iterator().next();
            job.cancelJob();
            unlockActions(job);
        }
        getMainFrame().setStatus("Cancelled");

    }

    public JobExecutor getJobExecutor() {
        return jobExecutor;
    }

    public ChooseStorageDialog getChooseStorageDialog() {
        return new ChooseStorageDialog(storageManager, mainFrame);
    }
}
