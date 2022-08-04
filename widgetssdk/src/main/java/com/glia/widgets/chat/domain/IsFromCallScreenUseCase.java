package com.glia.widgets.chat.domain;

import com.glia.widgets.chat.data.ChatScreenRepository;

public class IsFromCallScreenUseCase {
    private final ChatScreenRepository chatScreenRepository;

    public IsFromCallScreenUseCase(ChatScreenRepository chatScreenRepository) {
        this.chatScreenRepository = chatScreenRepository;
    }

    public boolean isFromCallScreen() {
        return chatScreenRepository.isFromCallScreen();
    }

}
