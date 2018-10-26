package net.zulkar.jb.core;

import com.typesafe.config.ConfigFactory;
import net.zulkar.jb.core.cache.CacheableStorage;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.ActionManager;
import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.action.ChangeStorageAction;
import net.zulkar.jb.core.ui.action.OpenAction;
import net.zulkar.jb.core.ui.action.ReloadAction;
import net.zulkar.jb.core.ui.action.SwitchAction;
import net.zulkar.jb.core.ui.render.SystemIconLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class FileManagerMain extends JFrame {
    private static final Logger log = LogManager.getLogger(FileManagerMain.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                SystemIconLoader iconLoader = new SystemIconLoader();
                StorageManager storageManager = new StorageManager();
                CacheableStorage initialStorage = getInitialStorage(storageManager);
                MainFrame frame = new MainFrame(iconLoader);
                ActionManager actionManager = new ActionManager(ConfigFactory.load());

                actionManager.init(new UiContext(frame, storageManager),
                        new OpenAction.Factory(),
                        new SwitchAction.Factory(),
                        new ChangeStorageAction.FactoryLeftPanel(),
                        new ChangeStorageAction.FactoryRightPanel(),
                        new ReloadAction.Factory()
                );
                frame.init(initialStorage, initialStorage, actionManager, new CleanTmpRunnable(storageManager, iconLoader));

                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            } catch (Exception e) {
                log.error(e);
            }

        });
    }

    private static CacheableStorage getInitialStorage(StorageManager storageManager) {
        CacheableStorage[] storages = storageManager.getAllAvailableStorages();
        if (storages == null || storages.length == 0) {
            throw new IllegalStateException("No storages available!");
        }
        return storages[0];
    }

    private static class CleanTmpRunnable implements Runnable {
        private final StorageManager storageManager;
        private final SystemIconLoader iconLoader;

        public CleanTmpRunnable(StorageManager storageManager, SystemIconLoader iconLoader) {
            this.storageManager = storageManager;
            this.iconLoader = iconLoader;
        }

        @Override
        public void run() {
            try {
                storageManager.close();
                iconLoader.close();
            } catch (Exception e) {
                log.error(e);
            }

        }
    }
}
