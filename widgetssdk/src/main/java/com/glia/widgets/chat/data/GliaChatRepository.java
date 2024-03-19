package com.glia.widgets.chat.data;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.widgets.chat.domain.GliaSendMessageUseCase.Listener;
import com.glia.widgets.chat.model.SendMessagePayload;
import com.glia.widgets.chat.model.Unsent;
import com.glia.widgets.di.GliaCore;

import java.util.function.Consumer;

/**
 * @hide
 */
public class GliaChatRepository {
    private final GliaCore gliaCore;

    public GliaChatRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    /**
     * @hide
     */
    public interface HistoryLoadedListener {
        void loaded(ChatMessage[] messages, Throwable error);
    }

    public void loadHistory(HistoryLoadedListener historyLoadedListener) {
        gliaCore.getChatHistory(historyLoadedListener::loaded);
    }

    public void listenForAllMessages(Consumer<ChatMessage> listener) {
        gliaCore.on(Glia.Events.CHAT_MESSAGE, listener);
    }

    public void unregisterAllMessageListener(Consumer<ChatMessage> listener) {
        gliaCore.off(Glia.Events.CHAT_MESSAGE, listener);
    }

    public void sendMessagePreview(String message) {
        gliaCore.getCurrentEngagement().ifPresent(value -> value.getChat().sendMessagePreview(message));
    }

    public void sendMessage(SendMessagePayload payload, RequestCallback<VisitorMessage> callback) {
        gliaCore.getCurrentEngagement().ifPresent(engagement -> engagement.getChat().sendMessage(payload.getPayload(), callback));
    }

    public void sendMessage(SendMessagePayload payload, Listener listener) {
        sendMessage(payload, (visitorMessage, ex) -> onMessageReceived(visitorMessage, ex, listener, payload));
    }

    private void onMessageReceived(VisitorMessage visitorMessage, GliaException ex, Listener listener, SendMessagePayload payload) {
        if (listener != null) {
            if (ex != null) {
                listener.error(ex, new Unsent(payload, ex));
            } else {
                listener.messageSent(visitorMessage);
            }
        }
    }
}
