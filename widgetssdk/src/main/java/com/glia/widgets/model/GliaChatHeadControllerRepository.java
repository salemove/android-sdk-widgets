package com.glia.widgets.model;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaChatHeadControllerRepository {

    private static final String TAG = "GliaChatHeadControllerRepository";

    private GliaChatHeadControllerRepositoryCallback callback;
    private final Consumer<ChatMessage> messageHandler = message -> {
        Logger.d(TAG, "newMessage");
        callback.onMessage();
    };
    private final Consumer<OperatorMediaState> operatorMediaStateConsumer = operatorMediaState -> {
        Logger.d(TAG, "operatorMediaState: " + operatorMediaState.toString());
        callback.newOperatorMediaState(operatorMediaState);
    };

    private final Consumer<OmnicoreEngagement> engagementHandler = engagement -> {
        Logger.d(TAG, "new engagement");
        engagement.getChat().on(Chat.Events.MESSAGE, messageHandler);
        engagement.getMedia().on(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
        callback.operatorDataLoaded(engagement.getOperator());
    };

    public void init(GliaChatHeadControllerRepositoryCallback callback) {
        this.callback = callback;
        Glia.on(Glia.Events.ENGAGEMENT, engagementHandler);
    }
}
