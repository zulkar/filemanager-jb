package net.zulkar.jb.core.ui.preview;

import net.zulkar.jb.core.domain.FileEntity;

import java.io.IOException;

public interface Previewer {
    void preview(FileEntity entity) throws IOException;

    boolean supports(FileEntity entity);
}
