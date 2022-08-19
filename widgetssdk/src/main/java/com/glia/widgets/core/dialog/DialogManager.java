package com.glia.widgets.core.dialog;

import androidx.annotation.NonNull;

import com.glia.widgets.core.dialog.model.DialogState;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public final class DialogManager {
    @NonNull
    private final Callback callback;
    private final Deque<DialogState> stateDeque;
    private final DialogState none = new DialogState(Dialog.MODE_NONE);

    public DialogManager(@NonNull Callback callback) {
        this.callback = callback;
        stateDeque = new LinkedBlockingDeque<>();
    }

    public void offer(DialogState state) {
        if (isDialogShown()) {
            emitNone();
        }

        stateDeque.removeLastOccurrence(state);
        stateDeque.offer(state);
        callback.emitDialog(state);
    }

    private DialogState remove() {
        return stateDeque.pollLast();
    }

    private DialogState getCurrent() {
        return stateDeque.peekLast();
    }

    private void showNext() {
        if (stateDeque.isEmpty()) return;

        DialogState state = getCurrent();

        if (state != null) {
            callback.emitDialog(state);
        }
    }

    private void emitNone() {
        callback.emitDialog(none);
    }

    public void dismissCurrent() {
        emitNone();
        remove();
        showNext();
    }

    public void dismissAll() {
        stateDeque.clear();
        emitNone();
    }

    @Dialog.Mode
    public int getCurrentMode() {
        DialogState state = getCurrent();
        if (state == null) return Dialog.MODE_NONE;

        return state.getMode();
    }

    public boolean isDialogShown() {
        return !stateDeque.isEmpty();
    }

    interface Callback {
        void emitDialog(DialogState state);
    }
}
