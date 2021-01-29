package com.glia.widgets.call;

import com.glia.widgets.model.DialogsState;

public interface CallViewCallback {

    void emitState(CallState callState);

    void emitDialog(DialogsState dialogsState);

    void handleFloatingChatHead(boolean show);

    void navigateToChat();
}
