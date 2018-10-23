package net.zulkar.jb.core.ui.render;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SystemIconLoaderTest {

    private static SystemIconLoader loader;

    @BeforeAll
    public static void before() throws IOException {
        loader = new SystemIconLoader();

    }

    @AfterAll
    public static void close() throws Exception {
        if (loader != null) {
            loader.close();
        }

    }


    @Test
    public void dirIconShouldNotBeNull() {
        assertNotNull(loader.getDirectoryIcon());
    }

    @Test
    public void shouldBeAbleToGetImagesForBothCase() {
        assertNotNull(loader.get("ext"));
        assertNotNull(loader.get("EXT"));
    }

}