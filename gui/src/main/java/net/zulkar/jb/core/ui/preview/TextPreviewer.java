package net.zulkar.jb.core.ui.preview;

import net.zulkar.jb.core.SystemUtils;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.ui.MainFrame;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static javax.swing.JOptionPane.NO_OPTION;

public class TextPreviewer extends AbstractPopupViewer {
    private static final long MAX_TEXT_SIZE = SystemUtils.getLongProperty("net.zulkar.jb.gui.maxtext.size", 2 * 1024 * 1024); //5MB
    private final static List<String> SUPPORTED_EXTENSIONS = Arrays.asList("txt", "log", "sh", "bat", "xml", "java", "kt", "csv");

    public TextPreviewer(MainFrame mainFrame) {
        super(mainFrame, SUPPORTED_EXTENSIONS);
    }


    public void preview(FileEntity entity) throws IOException {
        if (entity.getSize() > MAX_TEXT_SIZE) {
            int result = JOptionPane.showConfirmDialog(mainFrame,
                    String.format(
                            "File %s is too big to open (%s), do you really want to open it?",
                            entity.getName(), FileUtils.byteCountToDisplaySize(entity.getSize())
                    ),
                    "Open file?",
                    JOptionPane.YES_NO_OPTION
            );
            if (result == NO_OPTION) {
                return;
            }
        }
        super.preview(entity);
    }

    @Override
    protected Component createComponent(FileEntity entity) throws IOException {

        JTextArea area = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(area);
        area.setEditable(false);
        area.setText(IOUtils.toString(entity.openInputStream(), Charset.defaultCharset()));
        return scrollPane;
    }
}
