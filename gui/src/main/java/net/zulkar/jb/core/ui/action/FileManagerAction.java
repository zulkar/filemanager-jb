package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public abstract class FileManagerAction extends AbstractAction {

    private static final Logger log = LogManager.getLogger(FileManagerAction.class);
    protected final UiContext context;
    private final boolean ignoreLock;

    protected FileManagerAction(UiContext context) {
        this(context, false);
    }

    protected FileManagerAction(UiContext context, boolean ignoreLock) {

        this.context = context;
        this.ignoreLock = ignoreLock;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (!context.isActionsLocked() || ignoreLock) {
                doAction(e);
            } else {
                log.debug("Actions is locked for {}", this.getClass().getSimpleName());
            }
        } catch (IOException ex) {
            context.getMainFrame().setStatus("Exception: %s", ex);
        }
    }

    protected abstract void doAction(ActionEvent e) throws IOException;


    public interface Factory<T extends FileManagerAction> {
        T create(UiContext context);

        String getId();
    }

}
