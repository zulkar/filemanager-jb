package net.zulkar.jb.core.handlers.zip.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TreeNode<T> {

    private T t;
    private String name;
    private List<TreeNode<T>> children;

    public TreeNode(String name) {
        this.t = t;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }

    public void addWithoutDupCheck(TreeNode<T> child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public List<TreeNode<T>> getChildren() {
        if (children == null) {
            return null;
        }
        return Collections.unmodifiableList(children);
    }

    public TreeNode<T> find(Predicate<? super TreeNode<T>> predicate) {
        if (children == null) {
            return null;
        }
        return children.stream().filter(predicate).findFirst().orElse(null);
    }
}
