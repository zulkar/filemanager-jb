package net.zulkar.jb.core.ui.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.zulkar.jb.core.SystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

public class SystemIconLoader implements AutoCloseable, IconLoader {
    private static final Logger log = LogManager.getLogger(SystemIconLoader.class);
    private final static String PREFIX = "net.zulkar.jb-icon";
    private final File cacheDir;
    private final Cache<String, Icon> iconCache;
    private final Icon dirIcon;

    public SystemIconLoader() throws IOException {
        cacheDir = Files.createTempDirectory(PREFIX).toFile();
        File dirFile = new File(cacheDir, "dir");
        if (!dirFile.mkdir()) {
            dirIcon = null;
            log.error("cannot create icon for dir file");
        } else {
            dirIcon = FileSystemView.getFileSystemView().getSystemIcon(dirFile);
        }

        iconCache = CacheBuilder.<String, Icon>newBuilder()
                .maximumSize(SystemUtils.getLongProperty("net.zulkar.jb.gui.iconcache.size", 100))
                .build();
    }

    @Override
    public Icon getDirectoryIcon() {
        return dirIcon;
    }

    @Override
    public Icon get(String extension) {
        try {
            if (StringUtils.isEmpty(extension)) {
                return null;
            }
            String cacheKey = extension.toLowerCase();
            return iconCache.get(cacheKey, () -> loadIcon(cacheKey));
        } catch (ExecutionException e) {
            log.error(e);
            return null;
        }
    }

    private Icon loadIcon(String extension) throws IOException {
        log.debug("load icon for {}", extension);
        File icon = new File(cacheDir, "icon." + extension);
        if (!icon.createNewFile()) {
            log.error("cannot create icon for {}", extension);
            return null;
        }
        return FileSystemView.getFileSystemView().getSystemIcon(icon);
    }

    @Override
    public void close() throws Exception {
        FileUtils.deleteDirectory(cacheDir);
    }

    public static File[] getCacheDirectories() {
        return new File(System.getProperty("java.io.tmpdir")).listFiles((d, n) -> n.startsWith(PREFIX));
    }


}
