package com.glia.widgets.model;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaMessagesNotSeenRepository {

    private static final String TAG = "GliaMessagesNotSeenRepository";

    private GliaMessagesNotSeenRepositoryCallback callback;
    private final Consumer<ChatMessage> messageHandler = message -> {
        Logger.d(TAG, "newMessage");
        callback.onNewMessage(message);
    };

    private final Consumer<OmnicoreEngagement> engagementHandler = engagement -> {
        Logger.d(TAG, "new engagement");
        engagement.getChat().on(Chat.Events.MESSAGE, messageHandler);
    };

    public void init(GliaMessagesNotSeenRepositoryCallback callback) {
        this.callback = callback;
        Glia.on(Glia.Events.ENGAGEMENT, engagementHandler);
    }
}
