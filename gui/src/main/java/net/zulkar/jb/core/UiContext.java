package net.zulkar.jb.core;

import com.google.common.collect.Sets;
import net.zulkar.jb.core.jobs.CancellableBackgroundJob;
import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.preview.ImageViewer;
import net.zulkar.jb.core.ui.preview.Previewer;
import net.zulkar.jb.core.ui.preview.TextPreviewer;
import net.zulkar.jb.core.ui.preview.UnionPreviewer;

import javax.annotation.concurrent.GuardedBy;
import java.util.Set;

public class UiContext {

    private final MainFrame mainFrame;
    private final Previewer previewer;
    private final StorageManager storageManager;

    @GuardedBy("this")
    private final Set<CancellableBackgroundJob<?>> lockSets = Sets.newIdentityHashSet();

    public UiContext(MainFrame mainFrame, StorageManager storageManager) {
        this.mainFrame = mainFrame;
        previewer = new UnionPreviewer(new ImageViewer(mainFrame), new TextPreviewer(mainFrame));
        this.storageManager = storageManager;
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


    public synchronized void unlockActions(CancellableBackgroundJob<?> job) {
        lockSets.remove(job);
    }

    public synchronized void lockActions(CancellableBackgroundJob<?> job) {
        lockSets.add(job);
    }

    public synchronized boolean isActionsLocked() {
        return !lockSets.isEmpty();
    }

}
