package net.zulkar.jb.core.jobs;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.preview.Previewer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

public class OpenFileJob extends CancellableBackgroundJob<Void> {
    private static final Logger log = LogManager.getLogger(OpenFileJob.class);
    private final Storage storage;
    private final FileEntity entity;
    private final Previewer previewer;

    public OpenFileJob(UiContext context, Storage storage, FileEntity entity, Previewer previewer) {
        super(context, true);
        this.storage = storage;
        this.entity = entity;
        this.previewer = previewer;
    }

    @Override
    protected Void doJob() throws Exception {
        if (storage.needCache()) {
            try (InputStream ignore = entity.openInputStream()) {
            }
        }
        return null;
    }

    @Override
    protected void succeedEDT(Void result) throws Exception {
        previewer.preview(entity);
    }

    @Override
    protected void failedEDT(Exception e) throws Exception {
        log.error("Cannot open entity {}", entity);
        context.getMainFrame().setStatus("Cannot open entity %s ", entity.getAbsolutePath());
    }
}
