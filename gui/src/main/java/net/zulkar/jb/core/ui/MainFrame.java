package net.zulkar.jb.core.ui;

import net.zulkar.jb.core.StorageManager;
import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.render.FileListModel;
import net.zulkar.jb.core.ui.render.FileListPanel;
import net.zulkar.jb.core.ui.render.IconLoader;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainFrame extends JFrame {

    private FileListPanel leftPanel;
    private FileListPanel rightPanel;

    private FileListPanel activePanel;
    private JLabel statusBar;

    public MainFrame() throws IOException {
        super("FileManager");
    }

    public void init(IconLoader iconLoader,
                     UiContext uiContext,
                     ActionManager actionManager,
                     Runnable onClose) throws IOException {

        leftPanel = createFileListPanel("Left", actionManager, uiContext, iconLoader);
        rightPanel = createFileListPanel("Right", actionManager, uiContext, iconLoader);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);


        setFocusTraversalKeysEnabled(false);
        setMinimumSize(new Dimension(400, 400));
        statusBar = new JLabel(" ");

        this.add(statusBar, BorderLayout.SOUTH);

        this.pack();
        this.setLocationByPlatform(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        leftPanel.makeActive();

        setCloseListener(onClose);

        validate();
    }

    private void setCloseListener(Runnable onClose) {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose.run();
                super.windowClosed(e);
            }
        });
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

    public void setStatus(String status, Object... parameters) {
        if (StringUtils.isEmpty(status)) {
            status = " ";
        }
        String finalStatus = status;
        SwingUtilities.invokeLater(() -> statusBar.setText(String.format(finalStatus, parameters)));
    }

    public void makeActive(FileListPanel fileListPanel) {
        activePanel = fileListPanel;
    }

    private FileListPanel createFileListPanel(String panelName, ActionManager actionManager, UiContext uiContext, IconLoader iconLoader) throws IOException {
        Storage storage = getInitialStorage(uiContext.getStorageManager());
        //local storage - can be retrieved in EDT
        FileListModel.EntityData initialEntityData = new FileListModel.EntityData(storage.getRootEntity(), null, storage.getRootEntity().ls());
        FileListModel fileListModel = new FileListModel(iconLoader, initialEntityData);
        return new FileListPanel(panelName, fileListModel, actionManager, storage, uiContext);
    }

    private static Storage getInitialStorage(StorageManager storageManager) {
        Storage[] storages = storageManager.getAllAvailableStorages();
        if (storages == null || storages.length == 0) {
            throw new IllegalStateException("No storages available!");
        }
        return storages[0];
    }

}

