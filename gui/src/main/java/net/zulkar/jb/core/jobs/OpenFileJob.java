package net.zulkar.jb.core.jobs;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.ProxyFileEntity;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.preview.Previewer;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OpenFileJob extends CancellableBackgroundJob<byte[]> {
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
    protected byte[] doJob() throws Exception {
        return IOUtils.toByteArray(entity.openInputStream());
    }

    @Override
    protected void succeedEDT(byte[] result) throws Exception {
        previewer.preview(new CachedDataFileEntity(result));
    }

    @Override
    protected void failedEDT(Exception e) throws Exception {
        log.error("Cannot open entity {}", entity);
        context.getMainFrame().setStatus("Cannot open entity %s ", entity.getAbsolutePath());
    }

    private class CachedDataFileEntity extends ProxyFileEntity {
        private final byte[] result;

        public CachedDataFileEntity(byte[] result) {
            super(OpenFileJob.this.entity);
            this.result = result;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(result);
        }
    }
}
