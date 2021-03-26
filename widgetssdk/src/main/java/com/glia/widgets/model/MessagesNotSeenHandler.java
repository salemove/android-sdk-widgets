package com.glia.widgets.model;

import com.glia.androidsdk.chat.Chat;
import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.List;

public class MessagesNotSeenHandler {

    private final static String TAG = "MessagesNotSeenHandler";
    private int count = 0;
    private boolean isCounting = false;
    private final List<MessagesNotSeenHandlerListener> listeners = new ArrayList<>();
    private final GliaMessagesNotSeenRepository messagesNotSeenRepository;

    public MessagesNotSeenHandler(GliaMessagesNotSeenRepository messagesNotSeenRepository) {
        this.messagesNotSeenRepository = messagesNotSeenRepository;
    }

    public void init() {
        Logger.d(TAG, "init");
        messagesNotSeenRepository.init(message -> {
            if (isCounting && message.getSender() == Chat.Participant.OPERATOR) {
                emitCount(count + 1);
            }
        });
    }

    public void callOnBackClicked(boolean isChatInBackstack) {
        Logger.d(TAG, "callOnBackClicked");
        if (isChatInBackstack) {
            emitCount(0);
            isCounting = false;
        }
    }

    public void chatOnBackClicked() {
        Logger.d(TAG, "chatOnBackClicked");
        emitCount(0);
        isCounting = true;
    }

    public void callChatButtonClicked() {
        Logger.d(TAG, "callChatButtonClicked");
        emitCount(0);
        isCounting = false;
    }

    public void onNavigatedToChat() {
        Logger.d(TAG, "onNavigatedToChat");
        emitCount(0);
        isCounting = false;
    }

    public void addListener(MessagesNotSeenHandlerListener listener) {
        Logger.d(TAG, "addListener");
        this.listeners.add(listener);
        listener.onNewCount(count);
    }

    public void removeListener(MessagesNotSeenHandlerListener listener) {
        Logger.d(TAG, "removeListener");
        listeners.remove(listener);
    }

    private void emitCount(int newCount) {
        count = newCount;
        Logger.d(TAG, "emitCount: " + count);
        for (MessagesNotSeenHandlerListener listener : listeners) {
            listener.onNewCount(count);
        }
    }

    public void chatUpgradeOfferAccepted() {
        emitCount(0);
        isCounting = true;
    }

    public interface MessagesNotSeenHandlerListener {
        void onNewCount(int count);
    }
}
