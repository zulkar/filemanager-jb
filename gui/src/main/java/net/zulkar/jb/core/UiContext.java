package net.zulkar.jb.core;

import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.preview.ImageViewer;
import net.zulkar.jb.core.ui.preview.Previewer;
import net.zulkar.jb.core.ui.preview.TextPreviewer;
import net.zulkar.jb.core.ui.preview.UnionPreviewer;

public class UiContext {

    private final MainFrame mainFrame;
    private final Previewer previewer;
    private final StorageManager storageManager;

    public UiContext(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        previewer = new UnionPreviewer(new ImageViewer(mainFrame), new TextPreviewer(mainFrame));
        storageManager = new StorageManager();
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
}
