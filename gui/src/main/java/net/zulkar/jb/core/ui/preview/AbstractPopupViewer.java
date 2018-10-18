package net.zulkar.jb.core.ui.preview;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public abstract class AbstractPopupViewer implements Previewer {

    private final List<String> supportedExtensions;
    protected MainFrame mainFrame;

    protected AbstractPopupViewer(MainFrame mainFrame, List<String> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }


    @Override
    public void preview(FileEntity entity) throws IOException {
        Component component = createComponent(entity);
        showDialog(entity.getName(), component);
    }

    protected void showDialog(String name, Component component) {
        final JDialog dialog = new JDialog(mainFrame, name, true);
        dialog.getContentPane().add(component);
        dialog.setLocationRelativeTo(null);
        dialog.pack();
        dialog.setVisible(true);
    }

    protected abstract Component createComponent(FileEntity entity) throws IOException;


    @Override
    public boolean supports(FileEntity entity) {
        return supportedExtensions.contains(entity.getExtension().toLowerCase());
    }
}
