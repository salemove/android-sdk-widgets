package com.glia.widgets.core.dialog

import com.glia.widgets.core.dialog.model.DialogState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class DialogManagerTest {
    private lateinit var manager: DialogManager
    private lateinit var managerCallback: DialogManager.Callback

    @Before
    fun setUp() {
        managerCallback = mock()
        manager = DialogManager(managerCallback)
    }

    @Test
    fun addAndEmit_emmitCallbackOnce_ifStackIsEmpty() {
        manager.addAndEmit(DialogState.OverlayPermission)
        verify(managerCallback).emitDialog(DialogState.OverlayPermission)
    }

    @Test
    fun addAndEmit_emmitCallbackTwice_ifStackIsNotEmpty() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.addAndEmit(DialogState.OverlayPermission)
        val captor = argumentCaptor<DialogState>()
        verify(managerCallback, times(2)).emitDialog(captor.capture())
        val values = captor.allValues
        assertEquals(DialogState.None, values[0])
        assertEquals(DialogState.OverlayPermission, values[1])
    }

    @Test
    fun addAndEmit_addStateToStack_ifStackIsEmpty() {
        manager.addAndEmit(DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.size.toLong(), 1)
        assertEquals(
            manager.dialogStateDeque.removeFirst(),
            DialogState.OverlayPermission
        )
    }

    @Test
    fun addAndEmit_addStateToStack_ifStackIsNotEmpty() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.addAndEmit(DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.size.toLong(), 2)
        assertEquals(
            manager.dialogStateDeque.removeFirst(),
            DialogState.VisitorCode
        )
        assertEquals(
            manager.dialogStateDeque.removeFirst(),
            DialogState.OverlayPermission
        )
    }

    @Test
    fun addAndEmit_removeLastOccurrence_ifStateInStack() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        manager.dialogStateDeque.add(DialogState.Confirmation)
        manager.addAndEmit(DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.size.toLong(), 3)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.VisitorCode)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.Confirmation)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.OverlayPermission)
    }

    @Test
    fun add_emmitCallback_ifStackIsEmpty() {
        manager.add(DialogState.OverlayPermission)
        verify(managerCallback).emitDialog(eq(DialogState.OverlayPermission))
    }

    @Test
    fun add_notEmmitCallback_ifStackIsNotEmpty() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.add(DialogState.OverlayPermission)
        verify(managerCallback, never()).emitDialog(anyOrNull())
    }

    @Test
    fun add_addToStack_ifStackIsEmpty() {
        manager.add(DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.size.toLong(), 1)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.OverlayPermission)
    }

    @Test
    fun add_addToStack_ifStackIsNotEmpty() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.Confirmation)
        manager.add(DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.size.toLong(), 3)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.VisitorCode)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.Confirmation)
    }

    @Test
    fun add_removeLastOccurrence_ifStateInStack() {
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.Confirmation)
        manager.add(DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.size.toLong(), 3)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.VisitorCode)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.Confirmation)
    }

    @Test
    fun add_notChangeStack_ifNewStateEqualLastState() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.Confirmation)
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        manager.add(DialogState.OverlayPermission)
        assertEquals(manager.dialogStateDeque.size.toLong(), 3)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.VisitorCode)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.Confirmation)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.OverlayPermission)
    }

    @Test
    fun showNext_notEmmitCallbackOnce_ifStackEmpty() {
        manager.showNext()
        verify(managerCallback, never()).emitDialog(anyOrNull())
    }

    @Test
    fun showNext_emmitCallbackOnce_ifStackNotEmpty() {
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        manager.showNext()
        verify(managerCallback).emitDialog(DialogState.OverlayPermission)
    }

    @Test
    fun dismissCurrent_emmitCallbackWithNoneState_onExecute() {
        manager.dismissCurrent()
        verify(managerCallback).emitDialog(DialogState.None)
    }

    @Test
    fun dismissCurrent_emmitCallbackWithPreviousState_onExecute() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        manager.dismissCurrent()
        val captor = argumentCaptor<DialogState>()
        verify(managerCallback, times(2)).emitDialog(captor.capture())
        assertEquals(DialogState.VisitorCode, captor.lastValue)
    }

    @Test
    fun dismissCurrent_removeLastStateFromStack_onExecute() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        manager.dismissCurrent()
        assertEquals(manager.dialogStateDeque.size.toLong(), 1)
        assertEquals(manager.dialogStateDeque.removeFirst(), DialogState.VisitorCode)
    }

    @Test
    fun dismissAll_emmitCallbackWithNoneState_onExecute() {
        manager.dismissAll()
        verify(managerCallback).emitDialog(eq(DialogState.None))
    }

    @Test
    fun dismissAll_clearStack_onExecute() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        manager.dismissAll()
        assertTrue(manager.dialogStateDeque.isEmpty())
    }

    @Test
    fun currentMode_returnLastStackValue_ifStackIsNotEmpty() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        manager.dialogStateDeque.add(DialogState.OverlayPermission)
        assertEquals(DialogState.OverlayPermission, manager.currentDialogState)
    }

    @Test
    fun currentMode_returnLastStackValue_ifStackIsEmpty() {
        assertEquals(DialogState.None, manager.currentDialogState)
    }

    @Test
    fun isDialogShown_returnTrue_ifStackIsNotEmpty() {
        manager.dialogStateDeque.add(DialogState.VisitorCode)
        val result = manager.isDialogShown
        assertTrue(result)
    }

    @Test
    fun isDialogShown_returnFalse_ifStackIsEmpty() {
        val result = manager.isDialogShown
        assertFalse(result)
    }
}
