package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public abstract class FileManagerAction extends AbstractAction {

    protected final UiContext context;

    protected FileManagerAction(UiContext context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            doAction(e);
        } catch (IOException ex) {
            context.getMainFrame().setStatus(ex);
        }
    }

    protected abstract void doAction(ActionEvent e) throws IOException;


    public interface Factory<T extends FileManagerAction> {
        T create(UiContext context);

        String getId();
    }

}
