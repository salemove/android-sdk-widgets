package com.glia.widgets.chat.domain;

import androidx.annotation.Nullable;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.adapter.CustomCardAdapter;

public class CustomCardTypeUseCase {
    @Nullable
    private final CustomCardAdapter adapter;

    public CustomCardTypeUseCase(@Nullable CustomCardAdapter adapter) {
        this.adapter = adapter;
    }

    @Nullable
    public Integer execute(ChatMessage message) {
        if (adapter == null || message.getMetadata() == null) {
            return null;
        }
        return adapter.getChatAdapterViewType(message);
    }
}
