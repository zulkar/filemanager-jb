package net.zulkar.jb.core;

import com.google.common.annotations.VisibleForTesting;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocalStorage implements Storage {

    private Path rootDir;
    private List<ContainerHandler> containerHandlers;

    @VisibleForTesting
    LocalStorage(File root, List<ContainerHandler> containerHandlers) {
        rootDir = root.toPath();
        this.containerHandlers = containerHandlers;
    }

    @Override
    public FileEntity[] listFiles(String dir) throws IOException {
        return listFiles(new FileEntity(dir));
    }

    @Override
    public FileEntity[] listFiles(FileEntity dir) throws IOException {
        Validate.notNull(dir, "Directory is null");

        File[] ordinaryDirList = resolveToParent(dir).toFile().listFiles();

        if (ordinaryDirList != null) {
            return convert(ordinaryDirList);
        }
        File realFile = findRealFile(dir);
        if (realFile == null) {
            return null;
        }
        Optional<ContainerHandler> container = containerHandlers.stream().filter(ch -> ch.maySupport(this, realFile)).findFirst();
        return container.map(c -> c.listFiles(this, realFile, dir)).orElse(null);
    }

    private FileEntity[] convert(File[] files) {
        FileEntity[] result = new FileEntity[files.length];
        for (int i = 0; i < files.length; i++) {
            result[i] = new FileEntity(relativise(files[i].getAbsolutePath()));
        }
        return result;
    }

    private String relativise(String absolutePath) {
        return rootDir.relativize(Paths.get(absolutePath)).toString();
    }

    private FileEntity relativise(FileEntity entity) {
        return new FileEntity(rootDir.relativize(Paths.get(entity.getPath())).toString());
    }

    @Override
    public InputStream openInputStream(FileEntity entity) throws FileNotFoundException {
        Validate.notNull(entity, "Entity is null");
        File realFile = resolveToParent(entity).toFile();
        if (realFile.exists()) {
            return new FileInputStream(realFile);
        }

        File file = findRealFile(entity);
        if (file == null) {
            throw new FileNotFoundException(entity.getPath());
        }
        Optional<ContainerHandler> container = containerHandlers.stream().filter(ch -> ch.maySupport(this, file)).findFirst();
        return container.map(c -> c.readContent(this, file, relativise(entity))).orElseThrow(() -> new FileNotFoundException(entity.getPath()));
    }

    private Path resolveToParent(FileEntity entity) {
        String e = entity.getPath();
        Path resolved = rootDir.resolve(StringUtils.removeStart(e, "/"));
        return checkParent(resolved) ? resolved : rootDir;
    }

    private boolean checkParent(Path path) {
        try {
            String root = rootDir.toFile().getCanonicalPath();
            File file = path.toFile().getCanonicalFile();
            while (file != null) {
                if (root.equals(file.getCanonicalPath())) {
                    return true;
                }

                file = file.getParentFile();
            }
        } catch (IOException e) {
            return false;
        }


        return false;
    }


    @Override
    public String getName() {
        return rootDir.toString();
    }

    @Override
    public void close() throws Exception {
    }


    public static List<LocalStorage> localStorages(HandlerContext handlerContext) {
        return Arrays.stream(File.listRoots()).map(f -> new LocalStorage(f, handlerContext.getContainerHandlers())).collect(Collectors.toList());
    }


    private File findRealFile(FileEntity entity) {
        File file = resolveToParent(entity).toFile();

        while (!file.exists() && file != null) {
            file = file.getParentFile();
        }
        return file;
    }

}
