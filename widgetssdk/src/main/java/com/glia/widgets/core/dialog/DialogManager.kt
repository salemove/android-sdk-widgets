package com.glia.widgets.core.dialog

import androidx.annotation.VisibleForTesting
import com.glia.widgets.core.dialog.model.DialogState
import java.util.Deque
import java.util.concurrent.LinkedBlockingDeque

internal class DialogManager(private val callback: Callback) {
    @VisibleForTesting
    val dialogStateDeque: Deque<DialogState> by lazy { LinkedBlockingDeque() }

    private val current: DialogState? get() = dialogStateDeque.peekLast()
    val currentDialogState: DialogState get() = current ?: DialogState.None

    /**
     * @return true if the queue is not empty.
     */
    val isDialogShown: Boolean
        get() = !dialogStateDeque.isEmpty()

    /**
     * Used to add state to the queue.
     *
     * It will emit a new state immediately. The previous state is saved in the queue
     * and will be emitted after the [.dismissCurrent] is called.
     */
    fun addAndEmit(dialogState: DialogState) {
        if (isDialogShown) {
            emitNone()
        }
        dialogStateDeque.removeLastOccurrence(dialogState)
        dialogStateDeque.offer(dialogState)
        callback.emitDialog(dialogState)
    }

    /**
     * Used to add state to the queue.
     *
     * If the current [.getCurrentMode] state is not [Dialog.MODE_NONE] it will add
     * the state to the queue. The new state will emit after the [.dismissCurrent] called.
     *
     * If the current [.getCurrentMode] state is [Dialog.MODE_NONE]
     * it will emit a new state immediately.
     */
    fun add(dialogState: DialogState) {
        val isDialogShown = isDialogShown
        val currentState = remove()
        dialogStateDeque.removeFirstOccurrence(dialogState)
        dialogStateDeque.offer(dialogState)
        if (currentState != null && dialogState != currentState) {
            dialogStateDeque.offer(currentState)
        }
        if (!isDialogShown) {
            callback.emitDialog(dialogState)
        }
    }

    /**
     * Will remove specified dialog from the screen if it is currently being shown or from the queue.
     * If this dialog is not found, then nothing will happen
     */
    fun remove(dialogState: DialogState) {
        if (dialogStateDeque.isEmpty()) {
            // There are no dialogs that are being showed or queued
            return
        }
        if (dialogState == current) {
            dismissCurrent()
            return
        }
        dialogStateDeque.removeFirstOccurrence(dialogState)
    }

    private fun remove(): DialogState? {
        return dialogStateDeque.pollLast()
    }

    /**
     * Used to emit next DialogState.
     */
    fun showNext() {
        if (dialogStateDeque.isEmpty()) return
        val state = current
        if (state != null) {
            callback.emitDialog(state)
        }
    }

    private fun emitNone() {
        callback.emitDialog(DialogState.None)
    }

    /**
     * Used to remove the current state and emit the next state from the queue.
     */
    fun dismissCurrent() {
        emitNone()
        remove()
        showNext()
    }

    /**
     * Used to clear the queue and emit [Dialog.MODE_NONE] state.
     */
    fun dismissAll() {
        dialogStateDeque.clear()
        emitNone()
    }

    fun interface Callback {
        fun emitDialog(dialogState: DialogState)
    }
}
