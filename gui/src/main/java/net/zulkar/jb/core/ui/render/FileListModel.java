package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.time.format.FormatStyle.SHORT;

public class FileListModel extends AbstractTableModel {

    private Storage storage;
    private IconLoader loader;
    private FileEntity[] data;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(SHORT).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

    public FileListModel(IconLoader loader) {
        this.loader = loader;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }


    public void setPath(String path) throws IOException {
        data = storage.resolve(path).ls().toArray(new FileEntity[0]);
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.length + 1;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    public Class<?> getColumnClass(int c) {
        return c == 0 ? Icon.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (data == null || data.length <= rowIndex) {
            return null;
        }
        FileEntity entity = getEntity(rowIndex);

        switch (columnIndex) {
            case 0:
                return this.getIcon(entity);
            case 1:
                return entity.getName();
            case 2:
                return toSizeColumn(entity);
            case 3:
                return toDateColumn(entity.getModificationTime());
            default:
                return "";
        }
    }

    private Icon getIcon(FileEntity entity) {
        if (entity.isDir()) {
            return loader.getDirectoryIcon();
        }
        return loader.get(entity.getExtension());
    }

    private String toDateColumn(Instant dateTime) {
        return formatter.format(dateTime);
    }

    private String toSizeColumn(FileEntity entity) {
        if (entity == null) {
            return "???";
        }
        if (entity.isDir()) {
            return "<DIR>";
        }
        String hrSize = FileUtils.byteCountToDisplaySize(entity.getSize());
        if (entity.isContainer()) {
            return "<DIR> " + hrSize;
        }
        return hrSize;
    }

    public FileEntity getEntity(int selectedRow) {
        return data == null ? null : data[selectedRow];
    }
}
