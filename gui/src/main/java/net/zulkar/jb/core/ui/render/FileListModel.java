package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.domain.FileEntity;
import net.zulkar.jb.core.domain.FileEntityAttrs;
import net.zulkar.jb.core.domain.Storage;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class FileListModel extends AbstractTableModel {

    private Storage storage;
    private IconLoader loader;
    private FileEntity[] data;

    public FileListModel(IconLoader loader) {
        this.loader = loader;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }


    public void setPath(String path) throws IOException {
        data = storage.listFiles(path);
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
                return toSizeColumn(entity.getAttributes());
            case 3:
                return toDateColumn(entity.getAttributes());
            default:
                return "";
        }
    }

    private Icon getIcon(FileEntity entity) {
        if (entity.getAttributes() != null && entity.getAttributes().isDir()) {
            return loader.getDirectoryIcon();
        }
        return loader.get(entity.getExtension());
    }

    private String toDateColumn(FileEntityAttrs attrs) {
        if (attrs == null || attrs.getModificationTime() == null) {
            return "???";
        }
        return attrs.getModificationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String toSizeColumn(FileEntityAttrs attributes) {
        if (attributes == null) {
            return "???";
        }
        if (attributes.isDir()) {
            return "<DIR>";
        }
        String hrSize = FileUtils.byteCountToDisplaySize(attributes.getSize());
        if (attributes.isContainer()) {
            return "<DIR> " + hrSize;
        }
        return hrSize;
    }

    public FileEntity getEntity(int selectedRow) {
        if (selectedRow == 0) {
            return new FileEntity("..");
        }
        return data == null ? null : data[selectedRow - 1];
    }
}
