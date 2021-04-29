package com.glia.widgets.glia;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.model.GliaMessageRepository;

public class GliaLoadHistoryUseCase implements GliaMessageRepository.HistoryLoadedListener {

    public interface Listener {
        void historyLoaded(ChatMessage[] messages);

        void error(Throwable error);
    }

    private final GliaMessageRepository gliaMessageRepository;
    private Listener listener;

    public GliaLoadHistoryUseCase(GliaMessageRepository gliaMessageRepository) {
        this.gliaMessageRepository = gliaMessageRepository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        gliaMessageRepository.loadHistory(this);
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
