package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.jobs.ChangeDirJob;
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
    private final FileListModel model;
    private Storage storage;
    private final UiContext context;
    private final JTable table;
    private final JLabel storageLabel;
    private final String panelName;
    private final JTextField currentPathField;


    public FileListPanel(String panelName, FileListModel model, ActionManager actionManager, Storage initialStorage, UiContext context) throws IOException {
        this.panelName = panelName;
        this.model = model;
        storage = initialStorage;
        this.context = context;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        table = createTable();

        currentPathField = new JTextField();
        setToStatusPath(model.getCurrent());

        currentPathField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, currentPathField.getPreferredSize().height));
        storageLabel = new JLabel(initialStorage.getName());
        this.add(storageLabel);
        this.add(currentPathField);

        currentPathField.addActionListener(l -> {
            context.getJobExecutor().execute(new ChangeDirJob(context, this.storage, currentPathField.getText(), this));
            table.grabFocus();
        });
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        panel.add(new JScrollPane(table));


        registerActions(actionManager);

        this.add(panel);
        validate();
    }

    private JTable createTable() {
        JTable table = new JTable();
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(SINGLE_SELECTION);
        table.setModel(model);
        setFocusTraversalKeysEnabled(false);
        table.addFocusListener(new SelectionChangeFocusListener());
        table.setFocusTraversalKeysEnabled(false);
        return table;
    }

    private void registerActions(ActionManager actionManager) {
        disableDefaultActions();
        for (Map.Entry<KeyStroke, String> entry : actionManager.getInputMap().entrySet()) {
            table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(entry.getKey(), entry.getValue());
            table.getActionMap().put(entry.getValue(), actionManager.getActionMap().get(entry.getValue()));
        }

        table.addMouseListener(actionManager.getMouseListener());
    }

    private void disableDefaultActions() {
        ActionMap am = table.getActionMap();
        am.get("selectPreviousColumnCell").setEnabled(false);
        am.get("selectNextColumnCell").setEnabled(false);
        table.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        table.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    }


    public void setStorage(Storage storage) throws IOException {
        this.storage = storage;
        this.storageLabel.setText(storage.getName());
    }

    public Storage getStorage() {
        return storage;
    }

    public FileEntity getCurrentEntity() {
        if (table.getSelectedRow() == -1) {
            select(0);
        }
        return model.getEntity(table.getSelectedRow());
    }

    public FileListModel getModel() {
        return model;
    }


    private void select(int row) {
        if (row == -1 || row > table.getRowCount() - 1) {
            table.setRowSelectionInterval(0, 0);
            return;
        }
        table.setRowSelectionInterval(row, row);
    }

    public void makeActive() {
        table.grabFocus();
    }

    public String getPanelName() {
        return panelName;
    }

    public void setToStatusPath(FileEntity entity) {
        currentPathField.setText(storage.getSystemInternalPath(entity));
    }

    public class SelectionChangeFocusListener implements FocusListener {
        private int lastSelection = 0;

        @Override
        public void focusLost(FocusEvent e) {
            lastSelection = table.getSelectedRow();
            table.clearSelection();
        }

        @Override
        public void focusGained(FocusEvent e) {
            context.getMainFrame().makeActive(FileListPanel.this);
            if (lastSelection == -1) {
                lastSelection = 0;
            }
            select(lastSelection);
        }
    }

}
