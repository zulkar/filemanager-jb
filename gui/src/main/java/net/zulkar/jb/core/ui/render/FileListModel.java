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
    private FileEntity current;
    private FileEntity parent;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(SHORT).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

    public FileListModel(IconLoader loader, Storage storage) throws IOException {
        this.loader = loader;
        setStorage(storage);
    }

    public final void setStorage(Storage storage) throws IOException {
        this.storage = storage;
        setPath("/");
    }


    public void setPath(String path) throws IOException {
        current = storage.resolve(path);
        parent = current.getParent();
        data = current.ls().toArray(new FileEntity[0]);
    }

    @Override
    public int getRowCount() {
        if (data == null) {
            return 1;
        }
        return parent == null ? data.length : data.length + 1;
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
        if (data == null) {
            return null;
        }

        try {
            FileEntity entity = getEntity(rowIndex);
            if (entity == null) {
                return null;
            }

            switch (columnIndex) {
                case 0:
                    return this.getIcon(entity);
                case 1:
                    return toName(entity, rowIndex);
                case 2:
                    return toSizeColumn(entity);
                case 3:
                    return toDateColumn(entity.getModificationTime());
                default:
                    return "";
            }
        } catch (IOException e) {
            return null;
        }
    }

    private String toName(FileEntity entity, int rowIndex) throws IOException {
        if (parent != null && rowIndex == 0) {
            return "..";
        }
        return entity.getName();
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

    public FileEntity getEntity(int selectedRow) throws IOException {
        if (parent != null) {
            if (selectedRow == 0) {
                return parent;
            } else {
                return data[selectedRow - 1];
            }

        }
        return data[selectedRow];

    }
}
