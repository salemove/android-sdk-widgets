package com.glia.widgets.chat.data;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.MessageAttachment;
import com.glia.androidsdk.chat.OperatorTypingStatus;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.widgets.chat.domain.GliaSendMessageUseCase.Listener;
import com.glia.widgets.di.Dependencies;

public class GliaChatRepository {
    public interface HistoryLoadedListener {
        void loaded(ChatMessage[] messages, Throwable error);
    }

    public interface MessageListener {
        void onMessage(ChatMessage chatMessage);
    }

    public interface OperatorTypingListener {
        void onOperatorTyping(OperatorTypingStatus operatorTypingStatus);
    }

    public void loadHistory(HistoryLoadedListener historyLoadedListener) {
        Dependencies.glia().getChatHistory(historyLoadedListener::loaded);
    }

    public void listenForMessages(MessageListener listener, Engagement engagement) {
        engagement.getChat().on(Chat.Events.MESSAGE, listener::onMessage);
    }

    public void listenForOperatorTyping(OperatorTypingListener listener, Engagement engagement) {
        engagement.getChat().on(Chat.Events.OPERATOR_TYPING_STATUS, listener::onOperatorTyping);
    }

    public void unregisterMessageListener(MessageListener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement -> engagement.getChat().off(Chat.Events.MESSAGE, listener::onMessage));
    }

    public void unregisterOperatorTypingListener(OperatorTypingListener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement -> engagement.getChat().off(Chat.Events.OPERATOR_TYPING_STATUS, listener::onOperatorTyping));
    }

    public void sendMessagePreview(String message) {
        Dependencies.glia().getCurrentEngagement().ifPresent(value ->
                value.getChat().sendMessagePreview(message));
    }

    public void sendMessage(String message, Listener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener)));
    }

    public void sendMessageSingleChoice(SingleChoiceAttachment singleChoiceAttachment, Listener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(singleChoiceAttachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener)));
    }

    public void sendMessageWithAttachment(String message, MessageAttachment attachment, Listener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, attachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener))
        );
    }

    public void sendMessageAttachment(MessageAttachment attachment, Listener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage("", attachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener))
        );
    }

    public void sendResponse(String cardMessageId, String text, String value, Listener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendResponse(cardMessageId, text, value, (result, ex) -> {
                    if (listener != null) {
                        if (ex != null) {
                            listener.error(ex);
                        } else {
                            listener.messageSent(result.getVisitorResponseMessage());
                            listener.onCardMessageUpdated(result.getCardMessage());
                        }
                    }
                })
        );
    }

    private void onMessageReceived(VisitorMessage visitorMessage, GliaException ex, Listener listener) {
        if (listener != null) {
            if (ex != null) listener.error(ex);
            else listener.messageSent(visitorMessage);
        }
    }
}
