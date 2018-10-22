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
        return new ImagePanel(image);
    }

    @Override
    public boolean supports(FileEntity entity) {
        return SUPPORTED_EXTENSIONS.contains(entity.getExtension().toLowerCase());
    }

    private static class ImagePanel extends JPanel {

        private Image originalImage;
        private Image scaledImage;

        public ImagePanel(Image originalImage) {
            this.originalImage = originalImage;
        }

        @Override
        public void invalidate() {
            super.invalidate();
            int width = getWidth();
            int height = getHeight();

            if (width > 0 && height > 0) {
                scaledImage = originalImage.getScaledInstance(getWidth(), getHeight(),
                        Image.SCALE_DEFAULT);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return originalImage == null ? new Dimension(200, 200) : new Dimension(
                    originalImage.getWidth(this), originalImage.getHeight(this));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(scaledImage, 0, 0, null);
        }
    }

}
