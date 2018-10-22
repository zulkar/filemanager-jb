package net.zulkar.jb.core.ui.storage;

import net.zulkar.jb.core.StorageManager;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ftp.FtpParameters;
import net.zulkar.jb.core.ui.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.NumberFormat;

public class ChooseStorageDialog extends JDialog {
    private static final Logger log = LogManager.getLogger(ChooseStorageDialog.class);
    private final JList<Storage> jList;
    private final StorageManager storageManager;
    private volatile Storage chosenStorage;
    private final JTextField hostField;
    private final JTextField userField;
    private final JFormattedTextField portField;
    private final JTextField passwordField;

    public ChooseStorageDialog(StorageManager storageManager, MainFrame mainFrame) {
        super(mainFrame, true);
        this.storageManager = storageManager;
        jList = new JList<>(storageManager.getAllAvailableStorages());


        hostField = new JTextField();
        userField = new JTextField();
        portField = new JFormattedTextField(new MyPortFormatter());
        passwordField = new JPasswordField();


        JTabbedPane jtp = new JTabbedPane();
        jtp.addTab("Choose", initChoosePanel());
        jtp.addTab("new FTP", initFtpConnectionPanel());
        add(jtp);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(400, 400));
    }

    private JPanel initChoosePanel() {
        JPanel choosePanel = new JPanel();
        choosePanel.setLayout(new GridLayout(2, 1));

        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setCellRenderer(new StorageListRenderer());
        choosePanel.add(new JScrollPane(jList));
        JPanel buttonPanel = new JPanel();
        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(this::setChosenStorage);
        cancel.addActionListener(this::cancel);
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        choosePanel.add(buttonPanel);
        return choosePanel;
    }

    private void setChosenStorage(ActionEvent actionEvent) {
        chosenStorage = jList.getSelectedValue();
        this.dispose();
    }

    private JPanel initFtpConnectionPanel() {
        System.out.println("init - " + SwingUtilities.isEventDispatchThread());
        JPanel parametersPanel = new JPanel();
        parametersPanel.setLayout(new GridLayout(5, 2));

        parametersPanel.add(new JLabel("host"));
        parametersPanel.add(hostField);
        hostField.setMaximumSize(new Dimension(Integer.MAX_VALUE, hostField.getPreferredSize().height));

        parametersPanel.add(new JLabel("port"));
        parametersPanel.add(portField);

        parametersPanel.add(new JLabel("user"));
        parametersPanel.add(userField);

        parametersPanel.add(new JLabel("password"));
        parametersPanel.add(passwordField);

        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(this::createFtpStorage);
        cancel.addActionListener(this::cancel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        JPanel ftpPanel = new JPanel();
        ftpPanel.setLayout(new GridLayout(2, 1));
        ftpPanel.add(new JScrollPane(parametersPanel));
        ftpPanel.add(buttonPanel);
        return ftpPanel;
    }

    private void cancel(ActionEvent actionEvent) {
        this.chosenStorage = null;
        this.dispose();
    }

    private void createFtpStorage(ActionEvent actionEvent) {
        try {
            FtpParameters parameters = getFtpParameters();
            if (parameters == null) return;

            this.chosenStorage = storageManager.createFtpStorage(parameters);
            this.dispose();
        } catch (IOException e) {
            log.error(e);
            JOptionPane.showMessageDialog(this,
                    "Cannot create FTP storage : " + e.getMessage(),
                    "IO Exception",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private FtpParameters getFtpParameters() {
        Integer port = getPortValue();
        if (port == -1) {
            return null;
        }
        FtpParameters parameters = new FtpParameters();
        parameters.setHost(hostField.getText());
        parameters.setPort(port);
        parameters.setPassword(passwordField.getText());
        parameters.setUser(userField.getText());
        return parameters;
    }

    private int getPortValue() {
        int result = ((Number) portField.getValue()).intValue();
        if (result < 1 || result > 65535) {
            JOptionPane.showMessageDialog(this, "Port should be a number between 1 and 65535", "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        return result;
    }

    public Storage setChosenStorage() {
        setVisible(true);
        pack();
        return chosenStorage;
    }


    private static class StorageListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Storage) {
                setText(((Storage) value).getName());
            }
            return this;
        }
    }


    private static class MyPortFormatter extends NumberFormatter {
        public MyPortFormatter() {
            super(NumberFormat.getIntegerInstance());
            setValueClass(Integer.class);
            setAllowsInvalid(false);

            setMinimum(1);
            setMaximum(0xFFFF);
        }

    }
}
