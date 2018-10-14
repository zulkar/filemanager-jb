package net.zulkar.jb.core.handlers.zip;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class ZipArchiveFileEntityTest {

    @Test
    public void test() {
        String[] split = StringUtils.split("/a/b/c/", "/");
        System.out.println(split);
    }

}