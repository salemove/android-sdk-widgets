package com.glia.widgets.chat.domain;

import com.glia.widgets.chat.data.ChatScreenRepository;

public class UpdateFromCallScreenUseCase {
    private final ChatScreenRepository chatScreenRepository;

    public UpdateFromCallScreenUseCase(ChatScreenRepository chatScreenRepository) {
        this.chatScreenRepository = chatScreenRepository;
    }

    public void updateFromCallScreen(boolean fromCallScreen) {
        chatScreenRepository.setFromCallScreen(fromCallScreen);
    }
}
