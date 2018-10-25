package net.zulkar.jb.core.ui;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.ui.action.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ActionManagerTest {
    @Mock
    private UiContext uiContext;

    @Test
    public void allFactoriesSanityCheck() {
        Config testConf = ConfigFactory.load("application-test.conf");

        ActionManager actionManager = new ActionManager(testConf);

        actionManager.init(uiContext,
                new OpenAction.Factory(),
                new SwitchAction.Factory(),
                new ChangeStorageAction.FactoryLeftPanel(),
                new ChangeStorageAction.FactoryRightPanel(),
                new ReloadAction.Factory());

        checkActions(actionManager, new HashMap<Integer, Class<? extends FileManagerAction>>() {{
            put(KeyEvent.VK_ENTER, OpenAction.class);
            put(KeyEvent.VK_TAB, SwitchAction.class);
            put(KeyEvent.VK_F1, ChangeStorageAction.class);
            put(KeyEvent.VK_F2, ChangeStorageAction.class);
            put(KeyEvent.VK_R, ReloadAction.class);
        }});
    }

    private void checkActions(ActionManager actionManager, Map<Integer, Class<? extends FileManagerAction>> map) {
        for (Map.Entry<Integer, Class<? extends FileManagerAction>> entry : map.entrySet()) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(entry.getKey(), 0);
            String actionName = actionManager.getInputMap().get(keyStroke);
            assertNotNull(actionName);
            FileManagerAction action = actionManager.getActionMap().get(actionName);
            assertNotNull(action);
            assertTrue(entry.getValue().isAssignableFrom(action.getClass()));
        }

    }

}