package com.glia.widgets.core.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.glia.widgets.core.dialog.model.DialogState;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

public class DialogManagerTest {
    private DialogManager manager;
    private DialogManager.Callback managerCallback;

    @Before
    public void setUp() {
        managerCallback = mock(DialogManager.Callback.class);
        manager = new DialogManager(managerCallback);
    }

    @Test
    public void addAndEmit_emmitCallbackOnce_ifStackIsEmpty() {
        manager.addAndEmit(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        verify(managerCallback).emitDialog(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void addAndEmit_emmitCallbackTwice_ifStackIsNotEmpty() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));

        manager.addAndEmit(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        ArgumentCaptor<DialogState> captor = ArgumentCaptor.forClass(DialogState.class);
        verify(managerCallback, times(2)).emitDialog(captor.capture());

        List<DialogState> values = captor.getAllValues();
        assertEquals(new DialogState(Dialog.MODE_NONE), values.get(0));
        assertEquals(new DialogState(Dialog.MODE_OVERLAY_PERMISSION), values.get(1));
    }

    @Test
    public void addAndEmit_addStateToStack_ifStackIsEmpty() {
        manager.addAndEmit(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        assertEquals(manager.stateDeque.size(), 1);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void addAndEmit_addStateToStack_ifStackIsNotEmpty() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));

        manager.addAndEmit(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        assertEquals(manager.stateDeque.size(), 2);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void addAndEmit_removeLastOccurrence_ifStateInStack() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
        manager.stateDeque.add(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));

        manager.addAndEmit(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        assertEquals(manager.stateDeque.size(), 3);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void add_emmitCallback_ifStackIsEmpty() {
        manager.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        verify(managerCallback).emitDialog(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void add_notEmmitCallback_ifStackIsNotEmpty() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));

        manager.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        verify(managerCallback, never()).emitDialog(any());
    }

    @Test
    public void add_addToStack_ifStackIsEmpty() {
        manager.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        assertEquals(manager.stateDeque.size(), 1);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void add_addToStack_ifStackIsNotEmpty() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));

        manager.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        assertEquals(manager.stateDeque.size(), 3);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
    }

    @Test
    public void add_removeLastOccurrence_ifStateInStack() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));

        manager.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        assertEquals(manager.stateDeque.size(), 3);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
    }

    @Test
    public void add_notChangeStack_ifNewStateEqualLastState() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        manager.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        assertEquals(manager.stateDeque.size(), 3);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_NO_MORE_OPERATORS));
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void showNext_notEmmitCallbackOnce_ifStackEmpty() {
        manager.showNext();

        verify(managerCallback, never()).emitDialog(any());
    }

    @Test
    public void showNext_emmitCallbackOnce_ifStackNotEmpty() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        manager.showNext();

        verify(managerCallback).emitDialog(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));
    }

    @Test
    public void dismissCurrent_emmitCallbackWithNoneState_onExecute() {
        manager.dismissCurrent();

        verify(managerCallback).emitDialog(new DialogState(Dialog.MODE_NONE));
    }

    @Test
    public void dismissCurrent_emmitCallbackWithPreviousState_onExecute() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        manager.dismissCurrent();

        ArgumentCaptor<DialogState> captor = ArgumentCaptor.forClass(DialogState.class);
        verify(managerCallback, times(2)).emitDialog(captor.capture());
        assertEquals(new DialogState(Dialog.MODE_MEDIA_UPGRADE), captor.getValue());
    }

    @Test
    public void dismissCurrent_removeLastStateFromStack_onExecute() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        manager.dismissCurrent();

        assertEquals(manager.stateDeque.size(), 1);
        assertEquals(manager.stateDeque.removeFirst(), new DialogState(Dialog.MODE_MEDIA_UPGRADE));
    }

    @Test
    public void dismissAll_emmitCallbackWithNoneState_onExecute() {
        manager.dismissAll();

        verify(managerCallback).emitDialog(new DialogState(Dialog.MODE_NONE));
    }

    @Test
    public void dismissAll_clearStack_onExecute() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        manager.dismissAll();

        assertTrue(manager.stateDeque.isEmpty());
    }

    @Test
    public void getCurrentMode_returnLastStackValue_ifStackIsNotEmpty() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));
        manager.stateDeque.add(new DialogState(Dialog.MODE_OVERLAY_PERMISSION));

        int currentMode = manager.getCurrentMode();

        assertEquals(Dialog.MODE_OVERLAY_PERMISSION, currentMode);
    }

    @Test
    public void getCurrentMode_returnLastStackValue_ifStackIsEmpty() {
        int currentMode = manager.getCurrentMode();

        assertEquals(Dialog.MODE_NONE, currentMode);
    }

    @Test
    public void isDialogShown_returnTrue_ifStackIsNotEmpty() {
        manager.stateDeque.add(new DialogState(Dialog.MODE_MEDIA_UPGRADE));

        boolean result = manager.isDialogShown();

        assertTrue(result);
    }

    @Test
    public void isDialogShown_returnFalse_ifStackIsEmpty() {
        boolean result = manager.isDialogShown();

        assertFalse(result);
    }
}
