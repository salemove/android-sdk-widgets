package com.glia.widgets.internal.dialog

import com.glia.widgets.internal.dialog.model.DialogState
import java.util.Deque
import java.util.concurrent.LinkedBlockingDeque

internal class DialogManager(private val callback: Callback) {
    private val dialogStateDeque: Deque<DialogStateHolder> by lazy { LinkedBlockingDeque() }

    private val current: DialogState? get() = dialogStateDeque.peekLast()?.dialogState
    val currentDialogState: DialogState get() = current ?: DialogState.None

    /**
     * @return true if the queue is not empty.
     */
    private val isDialogShown: Boolean
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
        val wrapped = DialogStateHolder(dialogState)

        dialogStateDeque.removeLastOccurrence(wrapped)
        dialogStateDeque.offer(wrapped)
        callback.emitDialog(dialogState)
    }

    /**
     * Used to add state to the queue.
     *
     * If the current [.getCurrentMode] state is not [DialogState.None] it will add
     * the state to the queue. The new state will emit after the [.dismissCurrent] called.
     *
     * If the current [.getCurrentMode] state is [DialogState.None]
     * it will emit a new state immediately.
     */
    fun add(dialogState: DialogState) {
        val isDialogShown = isDialogShown
        val currentState = remove()
        val wrapped = DialogStateHolder(dialogState)
        dialogStateDeque.removeFirstOccurrence(wrapped)
        dialogStateDeque.offer(wrapped)
        if (currentState != null && wrapped != currentState) {
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
        dialogStateDeque.removeFirstOccurrence(DialogStateHolder(dialogState))
    }

    private fun remove(): DialogStateHolder? {
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
     * Used to clear the queue and emit [DialogState.None] state.
     */
    fun dismissAll() {
        dialogStateDeque.clear()
        emitNone()
    }

    fun interface Callback {
        fun emitDialog(dialogState: DialogState)
    }

    /**
     * Wrapping [DialogState] with this class will help
     * to make all the items with the same [DialogState] type but with different payloads equal for [DialogManager]
     *
     * For instance, we have [DialogState.MediaUpgrade] in [dialogStateDeque], to make [LinkedBlockingDeque.removeLastOccurrence] remove that state
     * without caring about [DialogState.MediaUpgrade] payload, we need it from [equals] to ignore the payload.
     * This class will help to make this happen.
     */
    private class DialogStateHolder(val dialogState: DialogState) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DialogStateHolder

            return dialogState::class == other.dialogState::class
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}
