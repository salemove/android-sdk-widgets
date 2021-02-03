package com.glia.widgets.di;

import android.app.Activity;

import com.glia.widgets.call.CallController;
import com.glia.widgets.call.CallViewCallback;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.chat.ChatController;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.helper.CallTimer;
import com.glia.widgets.helper.Logger;

public class ControllerFactory {

    private final RepositoryFactory repositoryFactory;
    private final CallTimer sharedTimer = new CallTimer();

    private static final String TAG = "ControllerFactory";

    private ChatController retainedChatController;
    private CallController retainedCallController;

    public ControllerFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public ChatController getChatController(Activity activity, ChatViewCallback chatViewCallback) {
        if (!(activity instanceof ChatActivity)) {
            Logger.d(TAG, "new");
            return new ChatController(repositoryFactory.getGliaChatRepository(), sharedTimer, chatViewCallback);
        }
        if (retainedChatController == null) {
            Logger.d(TAG, "new for chat activity");
            retainedChatController = new ChatController(
                    repositoryFactory.getGliaChatRepository(),
                    sharedTimer,
                    chatViewCallback);
        } else {
            Logger.d(TAG, "retained chat controller");
            retainedChatController.setViewCallback(chatViewCallback);
        }
        return retainedChatController;
    }

    public CallController getCallController(CallViewCallback callViewCallback) {
        if (retainedCallController == null) {
            Logger.d(TAG, "new call controller");
            retainedCallController = new CallController(
                    repositoryFactory.getGliaCallRepository(),
                    sharedTimer,
                    callViewCallback);
        } else {
            Logger.d(TAG, "retained call controller");
            retainedCallController.setViewCallback(callViewCallback);
        }
        return retainedCallController;
    }

    public void destroyControllers() {
        destroyCallController(false);
        destroyChatController();
    }

    public void destroyCallController(boolean retain) {
        Logger.d(TAG, "destroyCallController, retain: " + retain);
        if (retainedCallController != null) {
            retainedCallController.onDestroy(retain);
        }
        if (!retain) {
            retainedCallController = null;
        }
    }

    private void destroyChatController() {
        if (retainedChatController != null) {
            Logger.d(TAG, "destroyChatController");
            retainedChatController.onDestroy(false);
            retainedChatController = null;
        }
    }
}
