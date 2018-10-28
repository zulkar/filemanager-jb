package net.zulkar.jb.core.jobs;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.render.FileListModel;
import net.zulkar.jb.core.ui.render.FileListPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ChangeDirJob extends CancellableBackgroundJob<FileListModel.EntityData> {
    private static final Logger log = LogManager.getLogger(ChangeDirJob.class);
    private final Storage storage;
    private final String path;
    private final FileListPanel panel;

    public ChangeDirJob(UiContext context, Storage storage, String path, FileListPanel panel) {
        super(context, true);
        this.storage = storage;
        this.path = path;
        this.panel = panel;
    }

    @Override
    protected FileListModel.EntityData doJob() throws Exception {
        FileEntity resolved = storage.resolve(path);
        return new FileListModel.EntityData(resolved, resolved.getParent(), resolved.ls());
    }

    @Override
    protected void succeedEDT(FileListModel.EntityData result) throws IOException {
        panel.setToStatusPath(result.getCurrent());
        panel.getModel().setCurrentEntity(result);
        panel.makeActive();
    }

    @Override
    protected void failedEDT(Exception e) {
        log.error("Cannot resolve entity {} in {}", path, storage, e);
        context.getMainFrame().setStatus("Cannot resolve %s : %s", path, e.getMessage());
    }
}
