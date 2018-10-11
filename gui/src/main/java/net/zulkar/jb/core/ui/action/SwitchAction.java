package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.ui.MainFrame;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class SwitchAction extends FileManagerAction {

    protected SwitchAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void doAction(MainFrame mainFrame, ActionEvent e) throws IOException {
        System.out.println("focus changed");
        mainFrame.switchActivePanels();
    }


    public static class Factory implements FileManagerAction.Factory<SwitchAction> {
        @Override
        public SwitchAction create(MainFrame mainFrame) {
            return new SwitchAction(mainFrame);
        }

        @Override
        public String getId() {
            return "switch";
        }
    }

}
