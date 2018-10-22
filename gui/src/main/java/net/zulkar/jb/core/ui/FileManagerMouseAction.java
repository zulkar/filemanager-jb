package net.zulkar.jb.core.ui;

import net.zulkar.jb.core.ui.action.FileManagerAction;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class FileManagerMouseAction implements MouseListener {
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

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
