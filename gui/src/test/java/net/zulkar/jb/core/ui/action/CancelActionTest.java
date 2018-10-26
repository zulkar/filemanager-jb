package net.zulkar.jb.core.ui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;

class CancelActionTest extends FileManagerActionTest<CancelAction> {

    @BeforeEach
    void before() {
        super.init(new CancelAction.Factory());
    }

    @Test
    void shouldStop() {
        action.actionPerformed(event);
        verify(context).stopAllAndUnlock();
    }

}