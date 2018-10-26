package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class CancelAction extends FileManagerAction {
    public CancelAction(UiContext context) {
        super(context, true);
    }

    @Override
    protected void doAction(ActionEvent e) throws IOException {
        context.stopAndUnlock();
    }

    public static class Factory implements FileManagerAction.Factory<CancelAction> {
        @Override
        public CancelAction create(UiContext context) {
            return new CancelAction(context);
        }

        @Override
        public String getId() {
            return "cancel";
        }
    }

}
