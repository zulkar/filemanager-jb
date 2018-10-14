package net.zulkar.jb.core;

import java.io.File;

public class ResourcePathFinder {

    public static final File getResourceFile(String resourceName) {
        return new File(ResourcePathFinder.class.getClassLoader().getResource(resourceName).getFile());
    }
}
