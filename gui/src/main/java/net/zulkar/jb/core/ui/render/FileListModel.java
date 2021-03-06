package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.domain.FileEntity;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static java.time.format.FormatStyle.SHORT;
import static net.zulkar.jb.core.ui.render.FileEntityComparators.dirFirst;
import static net.zulkar.jb.core.ui.render.FileEntityComparators.nameIgnoreCase;

public class FileListModel extends AbstractTableModel {
    private static final Logger log = LogManager.getLogger(FileListModel.class);
    private static final String[] HEADERS = {"", "Name", "Size", "Modified"};
    private IconLoader loader;
    private FileEntity[] data;
    private FileEntity current;
    private FileEntity parent;
    private final Comparator<FileEntity> sortComparator;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(SHORT).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

    public FileListModel(IconLoader loader, EntityData entityData) throws IOException {
        this.loader = loader;
        sortComparator = dirFirst().thenComparing(nameIgnoreCase());
        setCurrentEntity(entityData);
    }


    public void setCurrentEntity(EntityData entityData) throws IOException {
        data = entityData.getChildren().toArray(new FileEntity[0]);
        Arrays.sort(data, sortComparator);
        this.current = entityData.getCurrent();
        this.parent = entityData.getParent();
        this.fireTableDataChanged();
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
            log.error("Exception getting {}:{}", rowIndex, columnIndex, e);
            return null;
        }
    }

    @Override
    public String getColumnName(int col) {
        return HEADERS[col];
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
        if (dateTime == null) {
            return "";
        }
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
        if (parent != null) {
            if (selectedRow == 0) {
                return parent;
            } else {
                return data[selectedRow - 1];
            }

        }
        return data[selectedRow];

    }

    public FileEntity getCurrent() {
        return current;
    }

    public static class EntityData {
        private final FileEntity current;
        private final FileEntity parent;
        private final List<FileEntity> children;

        public EntityData(FileEntity current, FileEntity parent, List<FileEntity> children) {
            this.current = current;
            this.parent = parent;
            this.children = children;
        }

        public FileEntity getCurrent() {
            return current;
        }

        public FileEntity getParent() {
            return parent;
        }

        public List<FileEntity> getChildren() {
            return children;
        }
    }

}
