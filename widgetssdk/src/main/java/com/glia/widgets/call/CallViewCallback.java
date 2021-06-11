package com.glia.widgets.call;

import com.glia.androidsdk.comms.MediaState;
import com.glia.widgets.model.DialogsState;

public interface CallViewCallback {

    void emitState(CallState callState);

    void navigateToChat();

    void startOperatorVideoView(MediaState operatorMediaState);

    void startVisitorVideoView(MediaState visitorMediaState);

    void switchSpeakerValue(boolean isSpeakerOn);
}
