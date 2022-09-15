package com.glia.widgets.chat.domain;

import androidx.annotation.Nullable;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.adapter.CustomCardAdapter;
import com.glia.widgets.chat.model.ChatInputMode;
import com.glia.widgets.chat.model.history.ChatItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomCardInteractableUseCase {
    @Nullable
    private final CustomCardAdapter adapter;

    public CustomCardInteractableUseCase(@Nullable CustomCardAdapter adapter) {
        this.adapter = adapter;
    }

    public ChatInputMode execute(List<ChatItem> currentChatItems, ChatMessage message) {
        if (adapter == null || message.getMetadata() == null) {
            return null;
        }
        return new ArrayList<>(currentChatItems).stream()
                .filter(chatItem -> message.getId().equals(chatItem.getId()))
                .map(chatItem -> adapter.getCustomCardViewType(chatItem.getViewType()))
                .filter(Objects::nonNull)
                .findFirst()
                .map(viewType -> {
                    if (adapter.isInteractable(message, viewType)) {
                        return ChatInputMode.SINGLE_CHOICE_CARD;
                    } else {
                        return ChatInputMode.ENABLED;
                    }
                })
                .orElse(null);
    }
}
