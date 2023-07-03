package com.glia.widgets.chat.data;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.MessageAttachment;
import com.glia.androidsdk.chat.OperatorTypingStatus;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.widgets.chat.domain.GliaSendMessageUseCase.Listener;
import com.glia.widgets.di.GliaCore;

import java.util.function.Consumer;

public class GliaChatRepository {
    private final GliaCore gliaCore;

    public GliaChatRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }
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
        gliaCore.getChatHistory(historyLoadedListener::loaded);
    }

    public void listenForEngagementMessages(MessageListener listener, Engagement engagement) {
        engagement.getChat().on(Chat.Events.MESSAGE, listener::onMessage);
    }

    public void listenForAllMessages(Consumer<ChatMessage> listener) {
        gliaCore.on(Glia.Events.CHAT_MESSAGE, listener);
    }

    public void listenForOperatorTyping(OperatorTypingListener listener, Engagement engagement) {
        engagement.getChat().on(Chat.Events.OPERATOR_TYPING_STATUS, listener::onOperatorTyping);
    }

    public void unregisterEngagementMessageListener(MessageListener listener) {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> engagement.getChat().off(Chat.Events.MESSAGE, listener::onMessage));
    }

    public void unregisterAllMessageListener(Consumer<ChatMessage> listener) {
        gliaCore.off(Glia.Events.CHAT_MESSAGE, listener);
    }

    public void unregisterOperatorTypingListener(OperatorTypingListener listener) {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> engagement.getChat().off(Chat.Events.OPERATOR_TYPING_STATUS, listener::onOperatorTyping));
    }

    public void sendMessagePreview(String message) {
        gliaCore.getCurrentEngagement().ifPresent(value ->
                value.getChat().sendMessagePreview(message));
    }

    public void sendMessage(String message, RequestCallback<VisitorMessage> callback) {
        gliaCore.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, callback));
    }

    public void sendMessage(String message, Listener listener) {
        gliaCore.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener)));
    }

    public void sendMessageSingleChoice(SingleChoiceAttachment singleChoiceAttachment, Listener listener) {
        gliaCore.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(singleChoiceAttachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener)));
    }

    public void sendMessageWithAttachment(String message, MessageAttachment attachment, RequestCallback<VisitorMessage> callback) {
        gliaCore.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, attachment, callback)
        );
    }

    public void sendMessageWithAttachment(String message, MessageAttachment attachment, Listener listener) {
        gliaCore.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, attachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener))
        );
    }

    public void sendMessageAttachment(MessageAttachment attachment, Listener listener) {
        gliaCore.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage("", attachment, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener))
        );
    }

    public void sendResponse(SingleChoiceAttachment attachment, RequestCallback<VisitorMessage> callback) {
        gliaCore.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(attachment, callback)
        );
    }

    private void onMessageReceived(VisitorMessage visitorMessage, GliaException ex, Listener listener) {
        if (listener != null) {
            if (ex != null) listener.error(ex);
            else listener.messageSent(visitorMessage);
        }
    }
}
