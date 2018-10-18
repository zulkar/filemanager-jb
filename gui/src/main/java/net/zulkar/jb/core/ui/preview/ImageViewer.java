package net.zulkar.jb.core.ui.preview;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.ui.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ImageViewer extends AbstractPopupViewer {

    private final static List<String> SUPPORTED_EXTENSIONS = Arrays.asList("jpeg", "jpg", "bmp", "png", "gif");

    public ImageViewer(MainFrame mainFrame) {
        super(mainFrame, SUPPORTED_EXTENSIONS);
    }


    @Override
    protected Component createComponent(FileEntity entity) throws IOException {
        BufferedImage image = ImageIO.read(entity.openInputStream());
        return new JLabel(new ImageIcon(image));
    }

    @Override
    public boolean supports(FileEntity entity) {
        return SUPPORTED_EXTENSIONS.contains(entity.getExtension().toLowerCase());
    }
}
