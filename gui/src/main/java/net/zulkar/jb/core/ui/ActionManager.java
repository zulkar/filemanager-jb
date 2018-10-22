package net.zulkar.jb.core.ui;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import net.zulkar.jb.core.UiContext;
import net.zulkar.jb.core.ui.action.FileManagerAction;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import static com.typesafe.config.ConfigValueType.STRING;

public class ActionManager {

    private final Config config;
    private final Map<KeyStroke, String> inputMap;
    private final Map<String, FileManagerAction> actionMap;

    public ActionManager(Config config) {
        this.config = config;
        inputMap = new HashMap<>();
        actionMap = new HashMap<>();
    }

    public void init(UiContext context, FileManagerAction.Factory<?>... factories) {
        for (FileManagerAction.Factory<?> factory : factories) {
            String actionId = factory.getId();
            FileManagerAction action = factory.create(context);
            actionMap.put(actionId, action);
        }

        for (Map.Entry<String, ConfigValue> entry : config.getConfig("keymap").entrySet()) {
            if (entry.getValue().valueType() != STRING) {
                throw new IllegalStateException("Bad config format: Look into documentation, but no documentation still. Look into code");
            }
            String actionId = (String) entry.getValue().unwrapped();
            if (actionMap.get(actionId) != null) {
                KeyStroke keyStroke = getKeyStroke(entry.getKey());
                if (keyStroke == null) {
                    throw new IllegalStateException(String.format("cannot parse keystroke from %s", entry.getKey()));
                }
                inputMap.put(keyStroke, actionId);
            }
        }

    }

    public Map<KeyStroke, String> getInputMap() {
        return inputMap;
    }

    public Map<String, FileManagerAction> getActionMap() {
        return actionMap;
    }

    private KeyStroke getKeyStroke(String id) {
        return KeyStroke.getKeyStroke(id);
    }

    public MouseListener getMouseListener() {
        return new FileManagerMouseAction(actionMap.get(config.getString("mouseсlick")));
    }
}
