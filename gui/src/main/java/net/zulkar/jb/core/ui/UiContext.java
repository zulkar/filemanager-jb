package net.zulkar.jb.core.ui;

import net.zulkar.jb.core.ui.preview.ImageViewer;
import net.zulkar.jb.core.ui.preview.Previewer;
import net.zulkar.jb.core.ui.preview.TextPreviewer;
import net.zulkar.jb.core.ui.preview.UnionPreviewer;

public class UiContext {

    private final MainFrame mainFrame;
    private final Previewer previewer;

    public UiContext(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        previewer = new UnionPreviewer(new ImageViewer(mainFrame), new TextPreviewer(mainFrame));
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public Previewer getPreviewer() {
        return previewer;
    }
}
