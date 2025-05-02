package com.glia.widgets.view;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.domain.GliaOnMessageUseCase;
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal;
import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @hide
 */
public class MessagesNotSeenHandler {

    private final static String TAG = "MessagesNotSeenHandler";
    private final GliaOnMessageUseCase gliaOnMessageUseCase;
    private final List<MessagesNotSeenHandlerListener> listeners = new ArrayList<>();
    private int count = 0;
    private boolean isCounting = false;
    private final Set<String> messageIds = new HashSet<>();

    public MessagesNotSeenHandler(GliaOnMessageUseCase gliaOnMessageUseCase) {
        this.gliaOnMessageUseCase = gliaOnMessageUseCase;
    }

    public void init() {
        Logger.d(TAG, "init");
        gliaOnMessageUseCase.invoke().doOnNext(this::onMessage).subscribe();
        isCounting = true;
    }

    public void chatOnBackClicked() {
        Logger.d(TAG, "chatOnBackClicked");
        reset();
        isCounting = true;
    }

    public void callChatButtonClicked() {
        Logger.d(TAG, "callChatButtonClicked");
        reset();
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
        reset();
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

    private void reset() {
        emitCount(0);
        messageIds.clear();
    }

    public void onMessage(ChatMessageInternal messageInternal) {
        if (isCounting && messageInternal.isNotVisitor() && isNew(messageInternal)) {
            emitCount(count + 1);
        }
    }

    private boolean isNew(ChatMessageInternal messageInternal) {
        return Optional.ofNullable(messageInternal)
            .map(ChatMessageInternal::getChatMessage)
            .map(ChatMessage::getId)
            .map(messageIds::add)
            .orElse(false);
    }

    public void onDestroy() {
        reset();
    }

    /**
     * @hide
     */
    public interface MessagesNotSeenHandlerListener {
        void onNewCount(int count);
    }
}
