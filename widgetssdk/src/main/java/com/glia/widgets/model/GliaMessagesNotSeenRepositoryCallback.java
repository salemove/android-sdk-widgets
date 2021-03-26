package com.glia.widgets.model;

import com.glia.androidsdk.chat.ChatMessage;

public interface GliaMessagesNotSeenRepositoryCallback {

    void onNewMessage(ChatMessage message);
}
