package net.zulkar.jb.core.ui.action;

import net.zulkar.jb.core.jobs.ChangeStorageJob;
import net.zulkar.jb.core.ui.storage.ChooseStorageDialog;
import net.zulkar.jb.core.ui.storage.StorageSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeStorageActionTest extends FileManagerActionTest<ChangeStorageAction> {

    @Mock
    private ChooseStorageDialog chooseDialog;
    @Mock
    private StorageSupplier supplier;

    @BeforeEach
    void before() {
        super.init(new ChangeStorageAction.FactoryRightPanel());
        lenient().when(context.getChooseStorageDialog()).thenReturn(chooseDialog);

    }

    @Test
    public void shouldNotDoAnythingIfSupplierIsNullCancelPressed() {
        when(chooseDialog.choose()).thenReturn(null);
        action.actionPerformed(event);
        verify(jobExecutor, never()).execute(any());
    }

    @Test
    public void shouldNotDoCreateStorageInEDT() throws IOException {
        when(chooseDialog.choose()).thenReturn(supplier);
        action.actionPerformed(event);
        verify(supplier, never()).get();
    }

    @Test
    public void shouldRunChangeStorageJob() throws IOException {
        when(chooseDialog.choose()).thenReturn(supplier);
        action.actionPerformed(event);
        verify(jobExecutor).execute(any(ChangeStorageJob.class));
    }

}