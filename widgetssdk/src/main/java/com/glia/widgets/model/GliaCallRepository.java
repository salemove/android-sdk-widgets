package com.glia.widgets.model;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.call.CallGliaCallback;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaCallRepository {

    private static final String TAG = "GliaCallRepository";
    private CallGliaCallback callback;
    private final Runnable engagementEndListener = () -> {
        if (callback != null) {
            callback.engagementEndedByOperator();
        }
    };
    private Consumer<ChatMessage> messageHandler;
    private final Consumer<OperatorMediaState> operatorMediaStateConsumer = operatorMediaState -> {
        Logger.d(TAG, "operatorMediaState: " + operatorMediaState.toString());
        callback.newOperatorMediaState(operatorMediaState);
    };
    private final Consumer<VisitorMediaState> visitorMediaStateConsumer = visitorMediaState -> {
        Logger.d(TAG, "visitorMediaState: " + visitorMediaState.toString());
        callback.newVisitorMediaState(visitorMediaState);
    };
    private final Consumer<OmnicoreEngagement> engagementHandler = engagement -> {
        callback.engagementSuccess(engagement);
        engagement.getChat().on(Chat.Events.MESSAGE, messageHandler);
        engagement.on(Engagement.Events.END, engagementEndListener);
        engagement.getMedia().on(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
        engagement.getMedia().on(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateConsumer);
    };

    public void init(CallGliaCallback callback) {
        this.callback = callback;
        messageHandler = callback::onMessage;
        Glia.on(Glia.Events.ENGAGEMENT, engagementHandler);
    }

    public void stop() {
        stopInternal();
    }

    public void onDestroy() {
        callback = null;
        Glia.off(Glia.Events.ENGAGEMENT, engagementHandler);
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getChat().off(Chat.Events.MESSAGE, messageHandler);
            engagement.off(Engagement.Events.END, engagementEndListener);
            engagement.getMedia().off(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
            engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateConsumer);
        });
    }

    private void stopInternal() {
        Glia.off(Glia.Events.ENGAGEMENT, engagementHandler);
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getChat().off(Chat.Events.MESSAGE, messageHandler);
            engagement.off(Engagement.Events.END, engagementEndListener);
            engagement.getMedia().off(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
            engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateConsumer);
            engagement.end(e -> {
                if (e != null) {
                    callback.error(e);
                }
            });
        });
    }
}
