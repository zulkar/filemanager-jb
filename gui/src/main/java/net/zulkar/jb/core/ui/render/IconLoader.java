package net.zulkar.jb.core.ui.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import net.zulkar.jb.core.SystemUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class IconLoader {
    private final File tempDir;
    private final Cache<String, Icon> iconCache;
    private final Icon dirIcon;

    public IconLoader() {
        tempDir = Files.createTempDir();
        File dirFile = new File(tempDir, "dir");
        if (!dirFile.mkdir()) {
            dirIcon = null;
        } else {
            dirIcon = FileSystemView.getFileSystemView().getSystemIcon(dirFile);
        }

        iconCache = CacheBuilder.<String, Icon>newBuilder()
                .maximumSize(SystemUtils.getLongProperty("net.zulkar.jb.gui.iconcache.size", 100))
                .build();

    }


    public Icon getDirectoryIcon() {
        return dirIcon;
    }

    public Icon get(String extension) {
        try {
            if (StringUtils.isEmpty(extension)){
                return null;
            }
            return iconCache.get(extension, () -> loadIcon(extension));
        } catch (ExecutionException e) {
            return null;
        }
    }

    private Icon loadIcon(String extension) throws IOException {
        File icon = new File(tempDir, "icon." + extension);
        icon.createNewFile();
        return FileSystemView.getFileSystemView().getSystemIcon(icon);
    }


}
