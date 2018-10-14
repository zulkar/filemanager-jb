package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.ui.MainFrame;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class OpenAction extends FileManagerAction {


    public OpenAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void doAction(MainFrame mainFrame, ActionEvent e) throws IOException {
        FileEntity entity = mainFrame.getActivePanel().getCurrentEntity();
        mainFrame.getActivePanel().cd(entity.getAbsolutePath());
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
