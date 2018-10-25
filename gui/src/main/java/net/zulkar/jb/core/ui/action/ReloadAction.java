package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.cache.CacheableStorage;
import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.render.FileListPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class ReloadAction extends FileManagerAction {

    private static final Logger log = LogManager.getLogger(OpenAction.class);

    protected ReloadAction(UiContext context) {
        super(context);
    }

    @Override
    protected void doAction(ActionEvent e) throws IOException {
        FileListPanel activePanel = context.getMainFrame().getActivePanel();
        FileEntity entity = activePanel.getCurrentEntity();
        Storage currentStorage = activePanel.getCurrentStorage();
        if (currentStorage instanceof CacheableStorage) {
            ((CacheableStorage) currentStorage).invalidate(entity.getAbsolutePath());
            activePanel.cd(entity.getAbsolutePath());
        } else {
            context.getMainFrame().setStatus("Storage %s is not realoadable", currentStorage.getName());
        }

    }
    public static class Factory implements FileManagerAction.Factory<ReloadAction> {
        @Override
        public ReloadAction create(UiContext context) {
            return new ReloadAction(context);
        }

        @Override
        public String getId() {
            return "reload";
        }
    }

}
