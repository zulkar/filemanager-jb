package net.zulkar.jb.core;

import net.zulkar.jb.core.domain.FileEntity;

import java.io.IOException;

public interface ContainerHandler {
    boolean maySupport(FileEntity file);

    FileEntity createFrom(FileEntity entity);

}
