package com.glia.widgets.view;

import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.chat.domain.GliaOnMessageUseCase;
import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.List;

public class MessagesNotSeenHandler implements
        GliaOnMessageUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener {

    private final GliaOnMessageUseCase gliaOnMessageUseCase;
    private final GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase;
    private final static String TAG = "MessagesNotSeenHandler";
    private int count = 0;
    private boolean isCounting = false;
    private final List<MessagesNotSeenHandlerListener> listeners = new ArrayList<>();

    public MessagesNotSeenHandler(
            GliaOnMessageUseCase gliaOnMessageUseCase,
            GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase
    ) {
        this.gliaOnMessageUseCase = gliaOnMessageUseCase;
        this.gliaOnEngagementEndUseCase = gliaOnEngagementEndUseCase;
    }

    public void init() {
        Logger.d(TAG, "init");
        gliaOnMessageUseCase.execute(this);
        gliaOnEngagementEndUseCase.execute(this);
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

    public void onChatWentBackground() {
        Logger.d(TAG, "onChatWentBackground");
        isCounting = true;
    }

    public void onNavigatedToCall() {
        Logger.d(TAG, "onNavigatedToCall");
        isCounting = true;
    }

    public void chatUpgradeOfferAccepted() {
        emitCount(0);
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

    @Override
    public void engagementEnded() {
        emitCount(0);
    }

    @Override
    public void onMessage(ChatMessage message) {
        if (isCounting && message.getSender() == Chat.Participant.OPERATOR) {
            emitCount(count + 1);
        }
    }

    public interface MessagesNotSeenHandlerListener {
        void onNewCount(int count);
    }
}
