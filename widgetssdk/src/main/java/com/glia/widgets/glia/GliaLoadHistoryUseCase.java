package com.glia.widgets.glia;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.helper.BaseObservable;
import com.glia.widgets.model.GliaMessageRepository;

public class GliaLoadHistoryUseCase extends BaseObservable<GliaLoadHistoryUseCase.Listener> implements GliaMessageRepository.Listener {

    public interface Listener {
        void historyLoaded(ChatMessage[] messages);

        void error(Throwable error);
    }

    private final GliaMessageRepository gliaMessageRepository;

    public GliaLoadHistoryUseCase(GliaMessageRepository gliaMessageRepository) {
        this.gliaMessageRepository = gliaMessageRepository;
    }

    public void execute() {
        gliaMessageRepository.loadHistory();
    }

    @Override
    public void loaded(ChatMessage[] messages, Throwable error) {
        if (error != null) {
            notifyFailure(error);
        } else {
            notifySuccess(messages);
        }
    }

    private void notifySuccess(ChatMessage[] messages) {
        for (Listener listener : getListeners()) {
            listener.historyLoaded(messages);
        }
    }

    private void notifyFailure(Throwable error) {
        for (Listener listener : getListeners()) {
            listener.error(error);
        }
    }

    @Override
    protected void onFirstListenerRegistered() {
        gliaMessageRepository.registerListener(this);
    }

    @Override
    protected void onLastListenerUnregistered() {
        gliaMessageRepository.unregisterListener(this);
    }
}
