package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.ui.MainFrame;
import net.zulkar.jb.core.ui.render.FileListPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class OpenAction extends FileManagerAction {

    private static final Logger log = LogManager.getLogger(FileListPanel.class);


    public OpenAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void doAction(MainFrame mainFrame, ActionEvent e) throws IOException {
        FileListPanel activePanel = mainFrame.getActivePanel();
        log.debug("active panel is {}", activePanel.getPanelName());
        FileEntity entity = activePanel.getCurrentEntity();
        log.debug("moving to {} at {}", entity, activePanel.getPanelName());
        activePanel.cd(entity.getAbsolutePath());
    }


    public static class Factory implements FileManagerAction.Factory<OpenAction> {
        @Override
        public OpenAction create(MainFrame mainFrame) {
            return new OpenAction(mainFrame);
        }

        @Override
        public String getId() {
            return "open";
        }
    }


}
