package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public abstract class FileManagerAction extends AbstractAction {

    protected final MainFrame mainFrame;

    protected FileManagerAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            doAction(mainFrame, e);
        } catch (IOException e1) {
            //todo
            e1.printStackTrace();
        }
    }

    protected abstract void doAction(MainFrame mainFrame, ActionEvent e) throws IOException;


    public interface Factory<T extends FileManagerAction> {
        T create(MainFrame mainFrame);
        String getId();
    }

}
