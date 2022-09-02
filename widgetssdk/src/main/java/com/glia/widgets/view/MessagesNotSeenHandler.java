package com.glia.widgets.view;

import com.glia.widgets.chat.domain.GliaOnMessageUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal;
import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.List;

public class MessagesNotSeenHandler implements GliaOnEngagementEndUseCase.Listener {

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
        gliaOnMessageUseCase.execute().doOnNext(this::onMessage).subscribe();
        gliaOnEngagementEndUseCase.execute(this);
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

    public void onMessage(ChatMessageInternal messageInternal) {
        if (isCounting && messageInternal.isOperator()) {
            emitCount(count + 1);
        }
    }

    public interface MessagesNotSeenHandlerListener {
        void onNewCount(int count);
    }
}
