package net.zulkar.jb.core.handlers.zip.archives;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.ProxyFileEntity;
import net.zulkar.jb.core.handlers.zip.LazyZipArchiveFileEntity;
import net.zulkar.jb.core.handlers.zip.ZipArchive;
import net.zulkar.jb.core.handlers.zip.ZipFileEntity;
import net.zulkar.jb.core.handlers.zip.ZipHandler;
import net.zulkar.jb.core.handlers.zip.tree.TreeNode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class InitializedZipArchive extends ProxyFileEntity implements ZipArchive {
    private final TreeNode<ZipEntry> root;
    private final ZipHandler zipHandler;
    private final LazyZipArchiveFileEntity wpapper;

    public InitializedZipArchive(FileEntity entity, TreeNode<ZipEntry> root, ZipHandler zipHandler, LazyZipArchiveFileEntity wpapper) {
        super(entity);
        this.root = root;
        this.zipHandler = zipHandler;
        this.wpapper = wpapper;
    }

    @Override
    public List<FileEntity> ls() {
        return Optional.ofNullable(ls("")).orElse(Collections.emptyList());
    }

    public List<FileEntity> ls(String path) {
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

    public FileEntity getParent(String path) {
        TreeNode<ZipEntry> node = root;
        String[] pathElements = StringUtils.split(path, "/");
        if (pathElements.length == 1) {
            return wpapper;
        }
        int i = 0;
        for (String pathElement : pathElements) {
            if (i++ < pathElements.length - 1) {
                node = node.find(n -> n.get() != null && n.getName().equals(pathElement));
            }
        }
        return fileEntityFrom(node);
    }

    private FileEntity fileEntityFrom(TreeNode<ZipEntry> node) {
        return zipHandler.createFrom(new ZipFileEntity(this, node.get()));
    }

    public InputStream openInputStream(ZipFileEntity entity) throws IOException {
        return zipHandler.readContent(entity);
    }

}
