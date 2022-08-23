package com.glia.widgets.core.dialog;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.glia.widgets.core.dialog.model.DialogState;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public final class DialogManager {
    @NonNull
    private final Callback callback;
    @VisibleForTesting
    final Deque<DialogState> stateDeque;
    private final DialogState none = new DialogState(Dialog.MODE_NONE);

    public DialogManager(@NonNull Callback callback) {
        this.callback = callback;
        stateDeque = new LinkedBlockingDeque<>();
    }

    /**
     * Used to add state to the queue.
     *
     * It will emit a new state immediately. The previous state is saved in the queue
     * and will be emitted after the {@link #dismissCurrent()} is called.
     */
    public void addAndEmit(@NonNull DialogState state) {
        if (isDialogShown()) {
            emitNone();
        }

        stateDeque.removeLastOccurrence(state);
        stateDeque.offer(state);
        callback.emitDialog(state);
    }

    /**
     * Used to add state to the queue.
     *
     * If the current {@link #getCurrentMode()} state is not {@link Dialog#MODE_NONE} it will add
     * the state to the queue. The new state will emit after the {@link #dismissCurrent()} called.
     *
     * If the current {@link #getCurrentMode()} state is {@link Dialog#MODE_NONE}
     * it will emit a new state immediately.
     */
    public void add(@NonNull DialogState state) {
        boolean isDialogShown = isDialogShown();
        DialogState currentState = remove();

        stateDeque.removeFirstOccurrence(state);
        stateDeque.offer(state);
        if (currentState != null && !state.equals(currentState)) {
            stateDeque.offer(currentState);
        }

        if (!isDialogShown) {
            callback.emitDialog(state);
        }
    }

    private DialogState remove() {
        return stateDeque.pollLast();
    }

    private DialogState getCurrent() {
        return stateDeque.peekLast();
    }

    /**
     * Used to emit next DialogState.
     */
    public void showNext() {
        if (stateDeque.isEmpty()) return;

        DialogState state = getCurrent();

        if (state != null) {
            callback.emitDialog(state);
        }
    }

    private void emitNone() {
        callback.emitDialog(none);
    }

    /**
     * Used to remove the current state and emit the next state from the queue.
     */
    public void dismissCurrent() {
        emitNone();
        remove();
        showNext();
    }

    /**
     * Used to clear the queue and emit {@link Dialog#MODE_NONE} state.
     */
    public void dismissAll() {
        stateDeque.clear();
        emitNone();
    }

    /**
     * @return the current mode of the {@link DialogState}.
     */
    @Dialog.Mode
    public int getCurrentMode() {
        DialogState state = getCurrent();
        if (state == null) return Dialog.MODE_NONE;

        return state.getMode();
    }

    /**
     * @return true if the queue is not empty.
     */
    public boolean isDialogShown() {
        return !stateDeque.isEmpty();
    }

    interface Callback {
        void emitDialog(DialogState state);
    }
}
