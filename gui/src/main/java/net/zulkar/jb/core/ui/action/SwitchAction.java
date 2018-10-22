package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class SwitchAction extends FileManagerAction {

    protected SwitchAction(UiContext context) {
        super(context);
    }

    @Override
    protected void doAction(ActionEvent e) throws IOException {
        context.getMainFrame().switchActivePanels();
    }


    public static class Factory implements FileManagerAction.Factory<SwitchAction> {
        @Override
        public SwitchAction create(UiContext context) {
            return new SwitchAction(context);
        }

        @Override
        public String getId() {
            return "switch";
        }
    }

}
