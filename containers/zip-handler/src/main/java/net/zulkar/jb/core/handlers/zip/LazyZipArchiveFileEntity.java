package net.zulkar.jb.core.handlers.zip;

import com.google.common.collect.Lists;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.ProxyFileEntity;
import net.zulkar.jb.core.handlers.zip.tree.TreeNode;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

class LazyZipArchiveFileEntity extends ProxyFileEntity {
    private ZipHandler zipHandler;

    private TreeNode<ZipEntry> root = new TreeNode<>(null, null);
    private boolean errorOnInit = false;
    private boolean initialized = false;

    LazyZipArchiveFileEntity(ZipHandler zipHandler, FileEntity archiveFile) {
        super(archiveFile);
        this.zipHandler = zipHandler;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    protected void init() {
        try {
            zipHandler.init(this, entity);
        } catch (IOException e) {
            errorOnInit = true;
        }
    }

    @Override
    public List<FileEntity> ls() {
        if (errorOnInit) {
            return null;
        }
        checkInit();
        return Optional.ofNullable(ls("")).orElse(Collections.emptyList());
    }

    private void checkInit() {
        if (!initialized) {
            init();
        }
    }

    void checkAllNodesHaveEntries() {
        if (root.getChildren() == null) {
            return; //empty archive

        }
        Stack<TreeNode<ZipEntry>> stack = new Stack<>();
        root.getChildren().forEach(stack::push);

        while (!stack.empty()) {
            TreeNode<ZipEntry> node = stack.pop();
            List<TreeNode<ZipEntry>> children = node.getChildren();
            if (children != null) {
                children.forEach(stack::push);
            }

            if (node.get() == null) {
                if (node.getChildren() == null) {
                    throw new IllegalStateException(String.format("zip archive at %s have incomplete entities list %s", getAbsolutePath(), calcNodePath(node)));
                }
                fillNodeForMissedEntities(node);
            }
        }
    }

    private void fillNodeForMissedEntities(TreeNode<ZipEntry> node) {
        node.set(new ZipEntry(calcNodePath(node)));
    }

    private String calcNodePath(TreeNode<ZipEntry> node) {
        List<String> paths= new ArrayList<>();
        while (node != root && node!=null) {
            paths.add(node.getName());
            node = node.getParent();
        }
        return StringUtils.join(Lists.reverse(paths), "/") + "/";
    }

    FileEntity getParent(String path) {
        TreeNode<ZipEntry> node = root;
        String[] pathElements = StringUtils.split(path, "/");
        if (pathElements.length == 1) {
            return this;
        }
        int i = 0;
        for (String pathElement : pathElements) {
            if (i++ < pathElements.length - 1) {
                node = node.find(n -> n.get() != null && n.getName().equals(pathElement));
            }
        }
        return fileEntityFrom(node);
    }

    List<FileEntity> ls(String path) {
        //todo - replace with descend
        TreeNode<ZipEntry> node = root;
        String[] pathElements = StringUtils.split(path, "/");
        for (String pathElement : pathElements) {
            node = node.find(n -> n.get() != null && n.getName().equals(pathElement));
            if (node == null) {
                throw new IllegalStateException(String.format("Cannot get children for %s, tree is incomplete. Internal error or wrong zip archive", path));
            }
        }
        if (node.getChildren() == null) {
            return null;
        }
        return node.getChildren().stream().map(this::fileEntityFrom).collect(Collectors.toList());
    }

    private FileEntity fileEntityFrom(TreeNode<ZipEntry> node) {
        return zipHandler.createFrom(new ZipFileEntity(this, node.get()));
    }

    InputStream openInputStream(ZipFileEntity entity) throws IOException {
        checkInit();
        return zipHandler.readContent(entity);
    }

    void add(ZipEntry e) {
        TreeNode<ZipEntry> node = getOrCreateNodeStructure(e.getName());
        node.set(e);
    }

    private TreeNode<ZipEntry> getOrCreateNodeStructure(String path) {
        TreeNode<ZipEntry> node = root;
        String[] pathElements = StringUtils.split(path, "/");
        for (String pathElement : pathElements) {
            node = getOrCreateChildNode(pathElement, node);
        }
        return node;
    }

    FileEntity getClosestFileEntry(String entityName) throws FileNotFoundException {
        TreeNode<ZipEntry> node = root;
        String[] pathElements = StringUtils.split(entityName, "/");
        for (String pathElement : pathElements) {
            TreeNode<ZipEntry> newNode = node.find(n -> n.get() != null && n.getName().equals(pathElement));
            if (newNode == null) {
                if (node.get().isDirectory()) {
                    throw new FileNotFoundException(String.format(getAbsolutePath() + "/" + entityName));
                }
                return fileEntityFrom(node);
            }
            node = newNode;
        }
        return fileEntityFrom(node);

    }

    private TreeNode<ZipEntry> getOrCreateChildNode(String pathElement, TreeNode<ZipEntry> parent) {
        TreeNode<ZipEntry> node = parent.find(n -> pathElement.equals(n.getName())); //todo - filenames case
        if (node == null) {
            node = new TreeNode<>(pathElement, parent);
            parent.addWithoutDupCheck(node);
        }
        return node;
    }

}
