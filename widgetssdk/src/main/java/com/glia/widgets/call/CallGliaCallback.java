package com.glia.widgets.call;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;

public interface CallGliaCallback {
    void error(GliaException e);

    void engagementEnded();

    void onMessage(ChatMessage message);

    void engagementSuccess(OmnicoreEngagement engagement);

    void engagementEndedByOperator();

    void newOperatorMediaState(OperatorMediaState operatorMediaState);
}
