package net.zulkar.jb.core.jobs;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.cache.CacheableStorage;
import net.zulkar.jb.core.ui.render.FileListPanel;
import net.zulkar.jb.core.ui.storage.StorageSupplier;

public class ChangeStorageJob extends CancellableBackgroundJob<CacheableStorage> {
    private final StorageSupplier storageSupplier;
    private final FileListPanel panel;

    public ChangeStorageJob(UiContext context, StorageSupplier storageSupplier, FileListPanel panel) {
        super(context, true);
        this.storageSupplier = storageSupplier;
        this.panel = panel;
    }

    @Override
    protected CacheableStorage doJob() throws Exception {
        return storageSupplier.get();
    }

    @Override
    protected void succeedEDT(CacheableStorage result) throws Exception {
        panel.setCurrentStorage(result);
    }

    @Override
    protected void failedEDT(Exception e) throws Exception {
        context.getMainFrame().setStatus("Exception while changing storage: %s", e.getMessage());
    }
}
