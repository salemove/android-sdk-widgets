package com.glia.widgets.glia;

import com.glia.widgets.model.GliaMessageRepository;

public class GliaSendMessagePreviewUseCase {

    private final GliaMessageRepository gliaMessageRepository;

    public GliaSendMessagePreviewUseCase(GliaMessageRepository gliaMessageRepository) {
        this.gliaMessageRepository = gliaMessageRepository;
    }

    public void execute(String message) {
        gliaMessageRepository.sendMessagePreview(message);
    }
}
