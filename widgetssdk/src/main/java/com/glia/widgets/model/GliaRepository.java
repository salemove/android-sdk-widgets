package com.glia.widgets.model;

import android.util.Log;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.VisitorContext;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.MediaDirection;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.chat.ChatGliaCallback;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaRepository {

    private static final String TAG = "GliaRepository";
    private ChatGliaCallback callback;
    private Consumer<QueueTicket> ticketConsumer;
    private Consumer<ChatMessage> messageHandler;
    private MediaUpgradeOffer offer;
    private final Runnable engagementEndListener = () -> {
        if (callback != null) {
            callback.engagementEndedByOperator();
        }
    };
    private final Consumer<OmnicoreEngagement> engagementHandler = engagement ->
            callback.engagementSuccess(engagement);
    private final Consumer<MediaUpgradeOffer> upgradeOfferConsumer = offer -> {
        this.offer = offer;
        // audio call
        if (offer.video == MediaDirection.NONE) {
            if (offer.audio != MediaDirection.TWO_WAY) {
                //offer.accept(resultCallback);
            } else {
                callback.audioUpgradeRequested();
                // Ask the visitor if they would like to share their camera and/or microphone media.
                // Once the visitor agrees call:
                // offer.accept(resultCallback);
            }
        }
    };
    private final Consumer<OperatorMediaState> operatorMediaStateConsumer = operatorMediaState -> {
        Logger.d(TAG, "operatorMediaState: " + operatorMediaState.toString());
    };
    private final Consumer<VisitorMediaState> visitorMediaStateConsumer = visitorMediaState -> {
        Logger.d(TAG, "visitorMediaState: " + visitorMediaState.toString());
    };
    private final RequestCallback<VisitorMessage> sendMessageCallback = (response, exception) -> {
        if (exception != null) {
            callback.error(exception);
        }
        if (response != null) {
            callback.messageDelivered(response);
        }
    };

    public void init(ChatGliaCallback callback, String queueId, String contextUrl) {
        this.callback = callback;
        callback.queueForEngagementStart();
        VisitorContext visitorContext = new VisitorContext(VisitorContext.Type.PAGE, contextUrl);
        Glia.on(Glia.Events.ENGAGEMENT, engagementHandler);
        Glia.queueForEngagement(queueId, visitorContext, response -> {
            if (response != null) {
                callback.error(response);
                return;
            }
            callback.queueForEngangmentSuccess();
        });
        ticketConsumer = ticket -> callback.queueForTicketSuccess(ticket.getId());
        Glia.on(Glia.Events.QUEUE_TICKET, ticketConsumer);
    }

    public void stop(String queueTicketId, boolean showDialog) {
        callback.engagementEnded(showDialog);
        stop(queueTicketId);
    }

    private void stop(String queueTicketId) {
        offer = null;
        if (queueTicketId != null) {
            Glia.cancelQueueTicket(queueTicketId, e -> {
                if (e != null) {
                    callback.error(e);
                }
            });
        }
        Glia.off(Glia.Events.ENGAGEMENT, engagementHandler);
        Glia.off(Glia.Events.QUEUE_TICKET, ticketConsumer);
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getChat().off(Chat.Events.MESSAGE, messageHandler);
            engagement.off(Engagement.Events.END, engagementEndListener);
            engagement.getMedia().off(Media.Events.MEDIA_UPGRADE_OFFER, upgradeOfferConsumer);
            engagement.getMedia().off(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
            engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateConsumer);
            engagement.end(e -> {
                if (e != null) {
                    callback.error(e);
                }
            });
        });
    }

    public void onDestroyView() {
        callback = null;
        offer = null;
        Glia.off(Glia.Events.ENGAGEMENT, engagementHandler);
        Glia.off(Glia.Events.QUEUE_TICKET, ticketConsumer);
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getChat().off(Chat.Events.MESSAGE, messageHandler);
            engagement.off(Engagement.Events.END, engagementEndListener);
            engagement.getMedia().off(Media.Events.MEDIA_UPGRADE_OFFER, upgradeOfferConsumer);
            engagement.getMedia().off(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
            engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateConsumer);
        });
    }

    public void sendMessagePreview(String message) {
        Glia.getCurrentEngagement().ifPresent(value ->
                value.getChat().sendMessagePreview(message));
    }

    public void sendMessage(String message) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, sendMessageCallback));
    }

    public void loadHistory() {
        Glia.getChatHistory(callback::chatHistoryLoaded);
    }

    public void initMessaging() {
        messageHandler = callback::onMessage;
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getChat().on(Chat.Events.MESSAGE, messageHandler);
            engagement.on(Engagement.Events.END, engagementEndListener);
            engagement.getMedia().on(Media.Events.MEDIA_UPGRADE_OFFER, upgradeOfferConsumer);
            engagement.getMedia().on(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
            engagement.getMedia().on(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateConsumer);
        });
    }

    public void acceptUpgradeOffer() {
        if (this.offer != null) {
            offer.accept(exception -> {
                if (exception == null) {
                    callback.audioUpgradeOfferChoiceSubmitSuccess();
                } else {
                    // TODO show upgrade error
                }
                offer = null;
            });
        }
    }

    public void declineOffer() {
        if (this.offer != null) {
            Log.d(TAG, "Decline");
            offer.decline(exception -> {
                Log.d(TAG, "Decline success");
                if (exception == null) {
                    callback.audioUpgradeOfferChoiceSubmitSuccess();
                } else {
                    Log.e(TAG, exception.toString());
                }
                offer = null;
            });
        }
    }
}
