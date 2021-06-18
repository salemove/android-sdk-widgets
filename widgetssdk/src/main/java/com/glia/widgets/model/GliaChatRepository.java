package com.glia.widgets.model;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;

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

    public void sendMessage(String message, RequestCallback<VisitorMessage> listener) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(message, listener));
    }

    public void sendMessage(SingleChoiceAttachment singleChoiceAttachment, RequestCallback<VisitorMessage> listener) {
        Glia.getCurrentEngagement().ifPresent(engagement ->
                engagement.getChat().sendMessage(singleChoiceAttachment, listener));
    }
}
