package net.zulkar.jb.core.ui.render;

import net.zulkar.jb.core.cache.CacheableStorage;
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
    private CacheableStorage storage;
    private IconLoader loader;
    private FileEntity[] data;
    private FileEntity current;
    private FileEntity parent;
    private final Comparator<FileEntity> sortComparator;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(SHORT).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

    public FileListModel(IconLoader loader, CacheableStorage storage) throws IOException {
        this.loader = loader;
        setStorage(storage);
        sortComparator = dirFirst().thenComparing(nameIgnoreCase());
        changeCurrent(storage.getRootEntity());
    }

    public final void setStorage(CacheableStorage storage) throws IOException {
        this.storage = storage;
    }


    public boolean setPath(String path) throws IOException {
        FileEntity newCurrent = storage.resolve(path);
        return setCurrentEntity(newCurrent);
    }

    public boolean setCurrentEntity(FileEntity result) throws IOException {
        if (result == null) {
            log.debug("cannot cd to {}", result);
            return false;
        }
        return changeCurrent(result);
    }

    private boolean changeCurrent(FileEntity newCurrent) throws IOException {
        List<FileEntity> newData = storage.ls(newCurrent);
        if (newData != null) {
            log.debug("setting path to {}", newCurrent.getAbsolutePath());
            parent = storage.getParent(newCurrent);
            data = newData.toArray(new FileEntity[0]);
            Arrays.sort(data, sortComparator);
            current = newCurrent;
            return true;
        }


        return false;
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

    public FileEntity getCurrent() {
        return current;
    }

    public CacheableStorage getStorage() {
        return storage;
    }

}
