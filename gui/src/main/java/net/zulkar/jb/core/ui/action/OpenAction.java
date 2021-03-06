package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.jobs.ChangeDirJob;
import net.zulkar.jb.core.jobs.OpenFileJob;
import net.zulkar.jb.core.ui.render.FileListPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;

public class OpenAction extends FileManagerAction {

    private static final Logger log = LogManager.getLogger(OpenAction.class);


    public OpenAction(UiContext context) {
        super(context);
    }

    @Override
    protected void doAction(ActionEvent e) {
        FileListPanel activePanel = context.getMainFrame().getActivePanel();
        log.debug("active panel is {}", activePanel.getPanelName());
        FileEntity entity = activePanel.getCurrentEntity();
        if (entity == null) {
            log.error("Cannot open: null");
            context.getMainFrame().setStatus("Cannot open: NULL");
        } else if (entity.isDir() || entity.isContainer()) {
            log.debug("moving to {} at {}", entity, activePanel.getPanelName());
            context.getJobExecutor().execute(new ChangeDirJob(context, activePanel.getStorage(), entity.getAbsolutePath(), activePanel));
        } else if (context.getPreviewer().supports(entity)) {
            context.getJobExecutor().execute(new OpenFileJob(context, activePanel.getStorage(), entity, context.getPreviewer()));
        } else {
            log.error("Cannot open: {}", entity.getName());
            context.getMainFrame().setStatus(String.format("Cannot open %s", entity.getName()));
        }
    }


    public static class Factory implements FileManagerAction.Factory<OpenAction> {
        @Override
        public OpenAction create(UiContext context) {
            return new OpenAction(context);
        }

        @Override
        public String getId() {
            return "open";
        }
    }


}
