package net.zulkar.jb.core.ui.preview;

import net.zulkar.jb.core.domain.FileEntity;

import java.io.IOException;

public class UnionPreviewer implements Previewer {

    private Previewer[] previewers;

    public UnionPreviewer(Previewer... previewers) {
        this.previewers = previewers;
    }


    @Override
    public void preview(FileEntity entity) throws IOException {
        for (Previewer previewer : previewers) {
            if (previewer.supports(entity)) {
                previewer.preview(entity);
                return;
            }
        }
        throw new IOException(String.format("Cannot preview %s", entity.getName()));
    }

    @Override
    public boolean supports(FileEntity entity) {
        for (Previewer previewer : previewers) {
            if (previewer.supports(entity)) {
                return true;
            }
        }
        return false;
    }
}
