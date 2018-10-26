package net.zulkar.jb.core.jobs;

import javafx.util.Pair;
import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.render.FileListModel;
import net.zulkar.jb.core.ui.render.FileListPanel;
import net.zulkar.jb.core.ui.storage.StorageSupplier;

public class ChangeStorageJob extends CancellableBackgroundJob<Pair<Storage, FileListModel.EntityData>> {
    private final StorageSupplier storageSupplier;
    private final FileListPanel panel;

    public ChangeStorageJob(UiContext context, StorageSupplier storageSupplier, FileListPanel panel) {
        super(context, true);
        this.storageSupplier = storageSupplier;
        this.panel = panel;
    }

    @Override
    protected Pair<Storage, FileListModel.EntityData> doJob() throws Exception {
        Storage storage = storageSupplier.get();
        FileEntity root = storage.getRootEntity();
        return new Pair<>(storage, new FileListModel.EntityData(root, null, root.ls()));

    }

    @Override
    protected void succeedEDT(Pair<Storage, FileListModel.EntityData> result) throws Exception {
        panel.setStorage(result.getKey());
        panel.getModel().setCurrentEntity(result.getValue());
    }

    @Override
    protected void failedEDT(Exception e) throws Exception {
        context.getMainFrame().setStatus("Exception while changing storage: %s", e.getMessage());
    }
}
