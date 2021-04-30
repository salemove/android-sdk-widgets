package com.glia.widgets.glia;

import com.glia.widgets.model.GliaChatRepository;

public class GliaSendMessagePreviewUseCase {

    private final GliaChatRepository gliaChatRepository;

    public GliaSendMessagePreviewUseCase(GliaChatRepository gliaChatRepository) {
        this.gliaChatRepository = gliaChatRepository;
    }

    public void execute(String message) {
        gliaChatRepository.sendMessagePreview(message);
    }
}
