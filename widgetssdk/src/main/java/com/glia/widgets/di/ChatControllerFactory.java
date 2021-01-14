package com.glia.widgets.di;

import android.app.Activity;

import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.chat.ChatController;
import com.glia.widgets.chat.ChatView;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.model.GliaRepository;

public class ChatControllerFactory {

    private ChatController retainedController;

    public ChatController getChatController(Activity activity, ChatViewCallback chatViewCallback) {
        if (!(activity instanceof ChatActivity)) {
            Logger.d("ChatControllerFactory", "new");
            return new ChatController(chatViewCallback);
        }
        if (retainedController == null) {
            Logger.d("ChatControllerFactory", "new for chat activity");
            retainedController = new ChatController(chatViewCallback);
        } else {
            Logger.d("ChatControllerFactory", "retained controller");
            retainedController.setViewCallback(chatViewCallback);
        }
        return retainedController;
    }

    public GliaRepository getGliaRepository() {
        return new GliaRepository();
    }

    public void destroyChatController(ChatView chatView) {
        this.retainedController = null;
    }
}
