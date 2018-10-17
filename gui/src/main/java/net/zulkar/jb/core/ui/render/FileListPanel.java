package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.ActionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Map;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class FileListPanel extends JPanel {

    private static final Logger log = LogManager.getLogger(FileListPanel.class);
    private FileListModel model;
    private JTable table;
    private String panelName;

    public FileListPanel(String panelName, IconLoader iconLoader, ActionManager actionManager, Storage initialStorage) throws IOException {
        this.panelName = panelName;
        this.model = new FileListModel(iconLoader, initialStorage);
        table = new JTable();
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(SINGLE_SELECTION);
        table.setModel(model);
        setFocusTraversalKeysEnabled(false);
        table.addFocusListener(new SelectionChangeFocusListener());
        table.setFocusTraversalKeysEnabled(false);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        registerActions(actionManager);
        this.add(new JScrollPane(table));
    }

    private void registerActions(ActionManager actionManager) {
        disableDefaultActions();
        for (Map.Entry<KeyStroke, String> entry : actionManager.getInputMap().entrySet()) {
            table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(entry.getKey(), entry.getValue());
            table.getActionMap().put(entry.getValue(), actionManager.getActionMap().get(entry.getValue()));
        }
    }

    private void disableDefaultActions() {
        ActionMap am = table.getActionMap();
        am.get("selectPreviousColumnCell").setEnabled(false);
        am.get("selectNextColumnCell").setEnabled(false);
        table.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        table.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    }


    public void setCurrentStorage(Storage storage) throws IOException {
        log.debug("set {} at panel {}", storage, panelName);
        model.setStorage(storage);
        cd("/");
    }

    public FileEntity getCurrentEntity() throws IOException {
        return model.getEntity(table.getSelectedRow());
    }

    public void cd(String path) throws IOException {
        log.debug("cd to {} in {}", path, panelName);
        model.setPath(path);
        model.fireTableDataChanged();
    }

    public void makeActive() {
        table.grabFocus();
    }

    public String getPanelName() {
        return panelName;
    }

    public class SelectionChangeFocusListener implements FocusListener {
        private int currentSelection = 0;

        @Override
        public void focusLost(FocusEvent e) {
            currentSelection = table.getSelectedRow();
            table.clearSelection();
        }

        @Override
        public void focusGained(FocusEvent e) {
            table.setRowSelectionInterval(currentSelection, currentSelection);
        }
    }


}
