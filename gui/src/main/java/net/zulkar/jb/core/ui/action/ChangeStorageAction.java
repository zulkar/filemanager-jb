package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.domain.Storage;
import net.zulkar.jb.core.ui.render.FileListPanel;
import net.zulkar.jb.core.ui.storage.ChooseStorageDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class ChangeStorageAction extends FileManagerAction {

    private static final Logger log = LogManager.getLogger(FileListPanel.class);
    private final Function<UiContext, FileListPanel> panelGetter;

    ChangeStorageAction(UiContext context, Function<UiContext, FileListPanel> panelGetter) {
        super(context);
        this.panelGetter = panelGetter;
    }

    @Override
    protected void doAction(ActionEvent e) throws IOException {
        List<Storage> storages = context.getStorageManager().getAllAvailableStorages();

        ChooseStorageDialog dialog = new ChooseStorageDialog(context.getStorageManager(), context.getMainFrame());
        Storage storage = dialog.setChosenStorage();
        if (storage != null) {
            panelGetter.apply(context).setCurrentStorage(storage);
        }
    }

    private static class LocalStorageRenderer implements ListCellRenderer<Storage> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Storage> list, Storage value, int index, boolean isSelected, boolean cellHasFocus) {
            return new JLabel(value.getName());
        }
    }

    public static class FactoryLeftPanel implements FileManagerAction.Factory<ChangeStorageAction> {
        @Override
        public ChangeStorageAction create(UiContext context) {
            return new ChangeStorageAction(context, ctx -> ctx.getMainFrame().getLeftPanel());
        }

        @Override
        public String getId() {
            return "choose left";
        }
    }

    public static class FactoryRightPanel implements FileManagerAction.Factory<ChangeStorageAction> {
        @Override
        public ChangeStorageAction create(UiContext context) {
            return new ChangeStorageAction(context, ctx -> ctx.getMainFrame().getRightPanel());
        }

        @Override
        public String getId() {
            return "choose right";
        }
    }
}
