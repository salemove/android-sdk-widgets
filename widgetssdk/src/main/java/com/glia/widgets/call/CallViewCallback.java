package com.glia.widgets.call;

public interface CallViewCallback {

    void emitState(CallState callState);

    void navigateToChat();
}
