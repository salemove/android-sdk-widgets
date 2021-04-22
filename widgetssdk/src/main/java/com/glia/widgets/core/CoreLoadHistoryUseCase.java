package com.glia.widgets.core;

import com.glia.androidsdk.chat.ChatMessage;

public class CoreLoadHistoryUseCase {

    private final CoreGliaRepository coreGliaRepository;

    public CoreLoadHistoryUseCase(CoreGliaRepository coreGliaRepository) {
        this.coreGliaRepository = coreGliaRepository;
    }

    public void execute(Callback callback) {
        coreGliaRepository.loadHistory((messages, error) -> {
            if (error != null) {
                callback.error(error);
            } else {
                callback.success(messages);
            }
        });
    }

    public interface Callback {
        void success(ChatMessage[] messages);

        void error(Throwable error);
    }
}
