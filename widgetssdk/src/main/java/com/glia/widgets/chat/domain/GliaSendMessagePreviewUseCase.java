package com.glia.widgets.chat.domain;

import com.glia.widgets.chat.data.GliaChatRepository;

public class GliaSendMessagePreviewUseCase {

    private final GliaChatRepository gliaChatRepository;

    public GliaSendMessagePreviewUseCase(GliaChatRepository gliaChatRepository) {
        this.gliaChatRepository = gliaChatRepository;
    }

    public void execute(String message) {
        gliaChatRepository.sendMessagePreview(message);
    }
}
