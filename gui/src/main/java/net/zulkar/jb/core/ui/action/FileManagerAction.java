package net.zulkar.jb.core.ui.action;

import com.google.common.annotations.VisibleForTesting;
import net.zulkar.jb.core.UiContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class FileManagerAction extends AbstractAction {

    private static final Logger log = LogManager.getLogger(FileManagerAction.class);
    protected final UiContext context;
    @VisibleForTesting
    final boolean ignoreLock;

    protected FileManagerAction(UiContext context) {
        this(context, false);
    }

    protected FileManagerAction(UiContext context, boolean ignoreLock) {

        this.context = context;
        this.ignoreLock = ignoreLock;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (!context.isActionsLocked() || ignoreLock) {
            doAction(e);
        } else {
            log.debug("Actions is locked for {}", this.getClass().getSimpleName());
        }

    }

    protected abstract void doAction(ActionEvent e);


    public interface Factory<T extends FileManagerAction> {
        T create(UiContext context);

        String getId();
    }

}
