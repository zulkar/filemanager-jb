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
    private boolean leftActive;

    public MainFrame() {
        super("FileManager");
    }

    public void init(Storage initialLeft, Storage initialRight, ActionManager actionManager) throws IOException {
        leftPanel = new FileListPanel(iconLoader, actionManager);
        rightPanel = new FileListPanel(iconLoader, actionManager);
        leftPanel.setCurrentStorage(initialLeft);
        rightPanel.setCurrentStorage(initialRight);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
        setFocusTraversalKeysEnabled(false);
        switchActivePanels();
        this.pack();
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public FileListPanel getActivePanel() {
        System.out.println("leftactive = " + leftActive);
        return leftActive ? leftPanel : rightPanel;
    }

    public FileListPanel getNonActivePanel() {
        return leftActive ? rightPanel : leftPanel;
    }

    public void switchActivePanels() {
        System.out.println("switching leftactive = " + leftActive);
        leftActive = !leftActive;
        leftPanel.makeActive(leftActive);
        rightPanel.makeActive(!leftActive);

    }

}
