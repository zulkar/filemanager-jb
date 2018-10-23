package net.zulkar.jb.core.local;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

interface LocalFileSystem {

    String pathToEntityModel(String absolutePath);

    String pathFromEntityModel(String entityPath, File root);
}

class WindowsFileSystem implements LocalFileSystem {

    @Override
    public String pathToEntityModel(String realFsPath) {
        String prefix = FilenameUtils.getPrefix(realFsPath);
        return "/" + FilenameUtils.normalizeNoEndSeparator(StringUtils.removeStart(realFsPath, prefix), true);
    }

    @Override
    public String pathFromEntityModel(String entityPath, File root) {
        return FilenameUtils.concat(root.getPath(), FilenameUtils.normalize(entityPath, false));
    }
}

class UnixFileSystem implements LocalFileSystem {

    @Override
    public String pathToEntityModel(String realFsPath) {
        return FilenameUtils.normalizeNoEndSeparator(realFsPath, true);
    }

    @Override
    public String pathFromEntityModel(String entityPath, File root) {
        return entityPath;
    }
}


final class LocalFileSystemFactory {

    private static final LocalFileSystem fs;

    static {
        if (StringUtils.containsIgnoreCase(System.getProperty("os.name"), "windows")) {
            fs = new WindowsFileSystem();
        } else {
            fs = new UnixFileSystem();
        }
    }

    private LocalFileSystemFactory() {
    }


    static LocalFileSystem getLocalFileSystem() {
        return fs;
    }

}
