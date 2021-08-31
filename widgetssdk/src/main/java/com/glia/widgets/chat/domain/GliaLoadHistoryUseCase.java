package com.glia.widgets.chat.domain;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.data.GliaChatRepository;

public class GliaLoadHistoryUseCase implements GliaChatRepository.HistoryLoadedListener {

    public interface Listener {
        void historyLoaded(ChatMessage[] messages);

        void error(Throwable error);
    }

    private final GliaChatRepository gliaChatRepository;
    private Listener listener;

    public GliaLoadHistoryUseCase(GliaChatRepository gliaChatRepository) {
        this.gliaChatRepository = gliaChatRepository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        gliaChatRepository.loadHistory(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public void loaded(ChatMessage[] messages, Throwable error) {
        if (listener != null) {
            if (error != null) {
                listener.error(error);
            } else {
                listener.historyLoaded(messages);
            }
        }
    }
}
