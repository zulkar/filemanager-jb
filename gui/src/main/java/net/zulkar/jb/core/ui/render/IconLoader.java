package net.zulkar.jb.core.ui.render;

import javax.swing.*;

public interface IconLoader {
    Icon getDirectoryIcon();

    Icon get(String extension);
}
