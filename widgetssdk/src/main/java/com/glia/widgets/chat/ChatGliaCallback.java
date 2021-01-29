package com.glia.widgets.chat;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;

public interface ChatGliaCallback {

    void queueForEngagementStart();

    void queueForEngangmentSuccess();

    void queueForTicketSuccess(String ticketId);

    void engagementEndedByOperator();

    void engagementEnded(boolean showDialog);

    void engagementSuccess(OmnicoreEngagement engagement);

    void onMessage(ChatMessage message);

    void chatHistoryLoaded(ChatMessage[] messages, Throwable error);

    void audioUpgradeRequested();

    void audioUpgradeOfferChoiceSubmitSuccess();

    void error(GliaException exception);

    void error(Throwable throwable);

    void messageDelivered(VisitorMessage visitorMessage);

    void audioUpgradeOfferChoiceDeniedSuccess();

    void newOperatorMediaState(OperatorMediaState operatorMediaState);
}
