package net.zulkar.jb.core;

import com.typesafe.config.ConfigFactory;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import net.zulkar.jb.core.local.LocalStorage;
import net.zulkar.jb.core.ui.ActionManager;
import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.action.ChangeStorageAction;
import net.zulkar.jb.core.ui.action.OpenAction;
import net.zulkar.jb.core.ui.action.SwitchAction;
import net.zulkar.jb.core.ui.render.SystemIconLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;

public class FileManagerMain extends JFrame {
    private static final Logger log = LogManager.getLogger(FileManagerMain.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LocalStorage storage = new LocalStorage(new ZipHandler(), new File("/"));
            try (SystemIconLoader iconLoader = new SystemIconLoader();
                 StorageManager storageManager = new StorageManager()) {
                MainFrame frame = new MainFrame(iconLoader);
                ActionManager actionManager = new ActionManager(ConfigFactory.load("application.conf"));

                actionManager.init(new UiContext(frame, storageManager),
                        new OpenAction.Factory(),
                        new SwitchAction.Factory(),
                        new ChangeStorageAction.FactoryLeftPanel(),
                        new ChangeStorageAction.FactoryRightPanel()
                );
                frame.init(storage, storage, actionManager);

                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            } catch (Exception e) {
                log.error(e);
            }

        });
    }

}
