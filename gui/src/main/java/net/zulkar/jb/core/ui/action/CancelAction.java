package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;

import java.awt.event.ActionEvent;

public class CancelAction extends FileManagerAction {
    public CancelAction(UiContext context) {
        super(context, true);
    }

    @Override
    protected void doAction(ActionEvent e) {
        context.stopAllAndUnlock();
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
