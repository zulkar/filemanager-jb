package net.zulkar.jb.core.handlers.zip;

import com.google.common.collect.Lists;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.handlers.zip.tree.TreeNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;

public class ZipTreeBuilder {
    private final TreeNode<ZipEntry> root = new TreeNode<>(null, null);
    private final FileEntity realEntity;

    public ZipTreeBuilder(FileEntity realEntity) {

        this.realEntity = realEntity;
    }

    public TreeNode<ZipEntry> getRoot() {
        checkAllNodesHaveEntries();
        return root;
    }

    public void addEntry(ZipEntry e) {
        TreeNode<ZipEntry> node = getOrCreateNodeStructure(e.getName());
        node.set(e);
    }

    private void checkAllNodesHaveEntries() {
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
                    throw new IllegalStateException(String.format("zip archive at %s have incomplete entities list %s", realEntity.getAbsolutePath(), calcNodePath(node)));
                }
                fillNodeForMissedEntities(node);
            }
        }
    }

    private void fillNodeForMissedEntities(TreeNode<ZipEntry> node) {
        node.set(new ZipEntry(calcNodePath(node)));
    }

    private String calcNodePath(TreeNode<ZipEntry> node) {
        List<String> paths = new ArrayList<>();
        while (node != root && node != null) {
            paths.add(node.getName());
            node = node.getParent();
        }
        return StringUtils.join(Lists.reverse(paths), "/") + "/";
    }


    private TreeNode<ZipEntry> getOrCreateNodeStructure(String path) {
        TreeNode<ZipEntry> node = root;
        String[] pathElements = StringUtils.split(path, "/");
        for (String pathElement : pathElements) {
            node = getOrCreateChildNode(pathElement, node);
        }
        return node;
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
