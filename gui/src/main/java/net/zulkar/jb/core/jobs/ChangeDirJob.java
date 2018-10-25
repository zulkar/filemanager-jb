package net.zulkar.jb.core.jobs;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.cache.CacheableStorage;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.ui.render.FileListPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ChangeDirJob extends CancellableBackgroundJob<FileEntity> {
    private static final Logger log = LogManager.getLogger(ChangeDirJob.class);
    private final CacheableStorage storage;
    private final String path;
    private final FileListPanel panel;

    public ChangeDirJob(UiContext context, CacheableStorage storage, String path, FileListPanel panel) {
        super(context, true);
        this.storage = storage;
        this.path = path;
        this.panel = panel;
    }

    @Override
    protected FileEntity doJob() throws Exception {
        return storage.ensureCached(path);
    }

    @Override
    protected void succeedEDT(FileEntity result) throws IOException {
        panel.cd(result.getAbsolutePath());
    }

    @Override
    protected void failedEDT(Exception e) {
        log.error("Cannot resolve entity {} in {}", path, storage, e);
        context.getMainFrame().setStatus("Cannot resolve %s : %s", path, e.getMessage());
    }
}
