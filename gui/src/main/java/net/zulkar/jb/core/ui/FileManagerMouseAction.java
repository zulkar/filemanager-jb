package net.zulkar.jb.core.ui;

import net.zulkar.jb.core.ui.action.FileManagerAction;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FileManagerMouseAction extends MouseAdapter {
    private FileManagerAction onclick;

    public FileManagerMouseAction(FileManagerAction onclick) {
        this.onclick = onclick;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            onclick.actionPerformed(null);
        }
    }
}
