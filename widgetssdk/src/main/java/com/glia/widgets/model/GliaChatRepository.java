package com.glia.widgets.model;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.MessageAttachment;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.widgets.glia.GliaSendMessageUseCase.Listener;

public class GliaChatRepository {
    public interface HistoryLoadedListener {
        void loaded(ChatMessage[] messages, Throwable error);
    }

    public interface MessageListener {
        void onMessage(ChatMessage chatMessage);
    }

    public void loadHistory(HistoryLoadedListener historyLoadedListener) {
        Glia.getChatHistory(historyLoadedListener::loaded);
    }

    public void listenForMessages(MessageListener listener, Engagement engagement) {
        engagement.getChat().on(Chat.Events.MESSAGE, listener::onMessage);
    }

    public void unregisterMessageListener(MessageListener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getChat().off(Chat.Events.MESSAGE, listener::onMessage);
        });
    }

    public void sendMessagePreview(String message) {
        Glia.getCurrentEngagement().ifPresent(value ->
                value.getChat().sendMessagePreview(message));
    }

    public void sendMessage(String message, Listener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener)));
    }

    public void sendMessage(SingleChoiceAttachment singleChoiceAttachment, Listener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(singleChoiceAttachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener)));
    }

    public void sendMessage(String message, MessageAttachment attachment, Listener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, attachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener))
        );
    }

    public void sendMessage(MessageAttachment attachment, Listener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(attachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener))
        );
    }

    private void onMessageReceived(VisitorMessage visitorMessage, GliaException ex, Listener listener) {
        if (listener != null) {
            if (ex != null) listener.error(ex);
            else listener.messageSent(visitorMessage);
        }
    }
}
