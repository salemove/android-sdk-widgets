package com.glia.widgets.internal.dialog

import com.glia.widgets.internal.dialog.model.DialogState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class DialogManagerTest {
    private lateinit var manager: DialogManager
    private lateinit var managerCallback: DialogManager.Callback

    @Before
    fun setUp() {
        managerCallback = mock()
        manager = DialogManager(managerCallback)
    }

    private fun fillDialogStack(vararg states: DialogState) {
        states.forEach(manager::add)
        verify(managerCallback).emitDialog(eq(states.first()))
        assertEquals(states.first(), manager.currentDialogState)
        clearInvocations(managerCallback)
    }

    @Test
    fun addAndEmit_emmitCallbackOnce_ifStackIsEmpty() {
        manager.addAndEmit(DialogState.OverlayPermission)
        verify(managerCallback).emitDialog(eq(DialogState.OverlayPermission))
        manager.dismissCurrent()
        verify(managerCallback).emitDialog(eq(DialogState.None))
        verifyNoMoreInteractions(managerCallback)
    }

    @Test
    fun addAndEmit_emmitCallbackTwice_ifStackIsNotEmpty() {
        fillDialogStack(DialogState.VisitorCode)
        manager.addAndEmit(DialogState.OverlayPermission)
        val captor = argumentCaptor<DialogState>()
        verify(managerCallback, times(2)).emitDialog(captor.capture())
        val values = captor.allValues
        assertEquals(DialogState.None, values[0])
        assertEquals(DialogState.OverlayPermission, values[1])
    }

    @Test
    fun addAndEmit_addStateToStack_ifStackIsNotEmpty() {
        fillDialogStack(DialogState.VisitorCode)
        manager.addAndEmit(DialogState.OverlayPermission)
        assertEquals(DialogState.OverlayPermission, manager.currentDialogState)
        verify(managerCallback).emitDialog(eq(DialogState.None))
        verify(managerCallback).emitDialog(eq(DialogState.OverlayPermission))

        manager.dismissCurrent()

        verify(managerCallback, times(2)).emitDialog(eq(DialogState.None))
        verify(managerCallback).emitDialog(eq(DialogState.VisitorCode))

        manager.dismissCurrent()

        verify(managerCallback, times(3)).emitDialog(eq(DialogState.None))
        verifyNoMoreInteractions(managerCallback)
    }

    @Test
    fun addAndEmit_removeLastOccurrence_ifStateInStack() {
        fillDialogStack(DialogState.VisitorCode, DialogState.OverlayPermission, DialogState.Confirmation)
        manager.addAndEmit(DialogState.OverlayPermission)
        assertEquals(DialogState.OverlayPermission, manager.currentDialogState)

        verify(managerCallback).emitDialog(eq(DialogState.None))
        verify(managerCallback).emitDialog(eq(DialogState.OverlayPermission))
        clearInvocations(managerCallback)

        manager.remove(DialogState.VisitorCode)
        manager.remove(DialogState.Confirmation)
        verify(managerCallback, never()).emitDialog(anyOrNull())

        manager.remove(DialogState.OverlayPermission)
        verify(managerCallback).emitDialog(eq(DialogState.None))
        assertEquals(DialogState.None, manager.currentDialogState)

        verifyNoMoreInteractions(managerCallback)
    }

    @Test
    fun add_emmitCallback_ifStackIsEmpty() {
        manager.add(DialogState.OverlayPermission)
        verify(managerCallback).emitDialog(eq(DialogState.OverlayPermission))
    }

    @Test
    fun add_notEmmitCallback_ifStackIsNotEmpty() {
        fillDialogStack(DialogState.VisitorCode)
        manager.add(DialogState.OverlayPermission)
        verify(managerCallback, never()).emitDialog(anyOrNull())
    }

    @Test
    fun add_addToStack_ifStackIsEmpty() {
        manager.add(DialogState.OverlayPermission)
        verify(managerCallback).emitDialog(eq(DialogState.OverlayPermission))
        assertEquals(manager.currentDialogState, DialogState.OverlayPermission)
    }

    @Test
    fun add_addToStack_ifStackIsNotEmpty() {
        val captor = argumentCaptor<DialogState>()
        fillDialogStack(DialogState.VisitorCode, DialogState.Confirmation)

        manager.add(DialogState.OverlayPermission)
        assertEquals(manager.currentDialogState, DialogState.VisitorCode)

        manager.remove(DialogState.Confirmation)
        manager.remove(DialogState.VisitorCode)
        verify(managerCallback, times(2)).emitDialog(captor.capture())

        val values = captor.allValues

        assertEquals(2, values.size)
        assertEquals(DialogState.None, values[0])
        assertEquals(DialogState.OverlayPermission, values[1])

        assertEquals(manager.currentDialogState, DialogState.OverlayPermission)
        verifyNoMoreInteractions(managerCallback)
    }

    @Test
    fun add_removeLastOccurrence_ifStateInStack() {
        fillDialogStack(DialogState.OverlayPermission, DialogState.VisitorCode, DialogState.Confirmation)

        manager.add(DialogState.OverlayPermission)
        assertEquals(manager.currentDialogState, DialogState.OverlayPermission)

        manager.remove(DialogState.VisitorCode)
        manager.remove(DialogState.Confirmation)
        manager.remove(DialogState.OverlayPermission)
        verify(managerCallback).emitDialog(eq(DialogState.None))

        assertEquals(DialogState.None, manager.currentDialogState)
    }

    @Test
    fun add_notChangeStack_ifNewStateEqualLastState() {
        fillDialogStack(DialogState.VisitorCode, DialogState.OverlayPermission, DialogState.Confirmation)
        manager.add(DialogState.VisitorCode)

        assertEquals(manager.currentDialogState, DialogState.VisitorCode)

        manager.dismissCurrent()
        verify(managerCallback).emitDialog(eq(DialogState.None))
        verify(managerCallback).emitDialog(eq(DialogState.Confirmation))

        manager.dismissCurrent()
        verify(managerCallback, times(2)).emitDialog(eq(DialogState.None))
        verify(managerCallback).emitDialog(eq(DialogState.OverlayPermission))

        verifyNoMoreInteractions(managerCallback)
    }

    @Test
    fun showNext_notEmmitCallbackOnce_ifStackEmpty() {
        manager.showNext()
        verify(managerCallback, never()).emitDialog(anyOrNull())
    }

    @Test
    fun showNext_emmitCallbackOnce_ifStackNotEmpty() {
        fillDialogStack(DialogState.OverlayPermission)
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
        fillDialogStack(DialogState.VisitorCode, DialogState.OverlayPermission)
        manager.dismissCurrent()
        val captor = argumentCaptor<DialogState>()
        verify(managerCallback, times(2)).emitDialog(captor.capture())
        assertEquals(DialogState.OverlayPermission, captor.lastValue)
    }

    @Test
    fun dismissCurrent_removeLastStateFromStack_onExecute() {
        fillDialogStack(DialogState.VisitorCode, DialogState.OverlayPermission)
        manager.dismissCurrent()
        assertEquals(DialogState.OverlayPermission, manager.currentDialogState)
    }

    @Test
    fun dismissAll_emmitCallbackWithNoneState_onExecute() {
        manager.dismissAll()
        verify(managerCallback).emitDialog(eq(DialogState.None))
        assertEquals(DialogState.None, manager.currentDialogState)
    }

    @Test
    fun dismissAll_clearStack_onExecute() {
        fillDialogStack(DialogState.VisitorCode, DialogState.OverlayPermission)
        manager.dismissAll()
        verify(managerCallback).emitDialog(eq(DialogState.None))
        assertEquals(DialogState.None, manager.currentDialogState)
    }

    @Test
    fun currentMode_returnLastStackValue_ifStackIsNotEmpty() {
        fillDialogStack(DialogState.VisitorCode, DialogState.OverlayPermission)
        assertEquals(DialogState.VisitorCode, manager.currentDialogState)
    }

    @Test
    fun currentMode_returnLastStackValue_ifStackIsEmpty() {
        assertEquals(DialogState.None, manager.currentDialogState)
    }
}
