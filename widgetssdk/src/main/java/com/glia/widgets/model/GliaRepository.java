package com.glia.widgets.model;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.VisitorContext;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.chat.ChatGliaCallback;

import java.util.function.Consumer;

public class GliaRepository {

    private ChatGliaCallback callback;
    private Consumer<QueueTicket> ticketConsumer;
    private Consumer<ChatMessage> messageHandler;
    private final Runnable engagementEndListener = () -> {
        if (callback != null) {
            callback.engagementEndedByOperator();
        }
    };
    private final Consumer<OmnicoreEngagement> engagementHandler = engagement ->
            callback.engagementSuccess(engagement);

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
            engagement.end(e -> {
                if (e != null) {
                    callback.error(e);
                }
            });
        });
    }

    public void onDestroyView() {
        callback = null;
        Glia.off(Glia.Events.ENGAGEMENT, engagementHandler);
        Glia.off(Glia.Events.QUEUE_TICKET, ticketConsumer);
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().off(Chat.Events.MESSAGE, messageHandler));
        Glia.getCurrentEngagement().ifPresent(engagement -> engagement.off(Engagement.Events.END, engagementEndListener));
    }

    public void sendMessagePreview(String message) {
        Glia.getCurrentEngagement().ifPresent(value ->
                value.getChat().sendMessagePreview(message));
    }

    public void sendMessage(String message) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, (response, exception) -> {
                    if (exception != null) {
                        callback.error(exception);
                    }
                }));
    }

    public void loadHistory() {
        Glia.getChatHistory(callback::chatHistoryLoaded);
    }

    public void initMessaging() {
        messageHandler = callback::onMessage;
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getChat().on(Chat.Events.MESSAGE, messageHandler);
            engagement.on(Engagement.Events.END, engagementEndListener);
        });
    }
}
