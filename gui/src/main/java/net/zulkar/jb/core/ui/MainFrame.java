package net.zulkar.jb.core.ui;

import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.render.FileListPanel;
import net.zulkar.jb.core.ui.render.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainFrame extends JFrame {

    private FileListPanel leftPanel;
    private FileListPanel rightPanel;

    private final IconLoader iconLoader = new IconLoader();
    private FileListPanel activePanel;

    public MainFrame() {
        super("FileManager");
    }

    public void init(Storage initialLeft, Storage initialRight, ActionManager actionManager) throws IOException {
        leftPanel = new FileListPanel("Left", iconLoader, actionManager, initialLeft, this);
        rightPanel = new FileListPanel("Right", iconLoader, actionManager, initialRight, this);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
        setFocusTraversalKeysEnabled(false);
        setMinimumSize(new Dimension(400, 400));
        this.pack();
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        leftPanel.makeActive();
    }


    public FileListPanel getLeftPanel() {
        return leftPanel;
    }

    public FileListPanel getRightPanel() {
        return rightPanel;
    }

    public FileListPanel getActivePanel() {
        return activePanel;
    }

    public void switchActivePanels() {
        if (rightPanel == activePanel) {
            activePanel = leftPanel;
        } else if (leftPanel == activePanel) {
            activePanel = rightPanel;
        } else {
            throw new IllegalStateException("Cannot find active panel!");
        }
        activePanel.makeActive();


    }

    public void setStatus(String status) {

    }

    public void makeActive(FileListPanel fileListPanel) {
        activePanel = fileListPanel;
    }
}
