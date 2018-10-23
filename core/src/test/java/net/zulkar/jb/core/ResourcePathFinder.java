package net.zulkar.jb.core;

import java.io.File;

public class ResourcePathFinder {

    public static final File getResourceFile(String resourceName) {
        return new File(ResourcePathFinder.class.getClassLoader().getResource(resourceName).getFile());
    }


    public static File getRootDir(String resourceName) {
        File file = getResourceFile(resourceName);
        while (file.getParentFile() != null) {
            file = file.getParentFile();
        }
        return file.getAbsoluteFile();
    }
}
