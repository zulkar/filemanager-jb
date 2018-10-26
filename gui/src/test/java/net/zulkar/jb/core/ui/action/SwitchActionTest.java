package net.zulkar.jb.core.ui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;

class SwitchActionTest extends FileManagerActionTest<SwitchAction> {
    @BeforeEach
    void before() {
        super.init(new SwitchAction.Factory());
    }

    @Test
    void shouldSwitch() {
        action.actionPerformed(event);
        verify(mainFrame).switchActivePanels();
    }


}