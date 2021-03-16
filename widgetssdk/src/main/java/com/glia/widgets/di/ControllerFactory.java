package com.glia.widgets.di;

import android.app.Activity;

import com.glia.widgets.call.CallController;
import com.glia.widgets.call.CallViewCallback;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.chat.ChatController;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.head.ChatHeadsController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.model.MinimizeHandler;
import com.glia.widgets.screensharing.ScreenSharingController;

public class ControllerFactory {

    private final RepositoryFactory repositoryFactory;
    private final TimeCounter sharedTimer = new TimeCounter();
    private final MinimizeHandler minimizeHandler = new MinimizeHandler();
    private final ChatHeadsController chatHeadsController;

    private static final String TAG = "ControllerFactory";

    private ChatController retainedChatController;
    private CallController retainedCallController;
    private ScreenSharingController retainedScreenSharingController;

    public ControllerFactory(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
        chatHeadsController = new ChatHeadsController(
                repositoryFactory.getGliaChatHeadControllerRepository()
        );
    }

    public ChatController getChatController(Activity activity, ChatViewCallback chatViewCallback) {
        if (!(activity instanceof ChatActivity)) {
            Logger.d(TAG, "new");
            return new ChatController(
                    repositoryFactory.getGliaChatRepository(),
                    repositoryFactory.getMediaUpgradeOfferRepository(),
                    sharedTimer,
                    chatViewCallback,
                    minimizeHandler,
                    chatHeadsController);
        }
        if (retainedChatController == null) {
            Logger.d(TAG, "new for chat activity");
            retainedChatController = new ChatController(
                    repositoryFactory.getGliaChatRepository(),
                    repositoryFactory.getMediaUpgradeOfferRepository(),
                    sharedTimer,
                    chatViewCallback,
                    minimizeHandler,
                    chatHeadsController);
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
                    repositoryFactory.getMediaUpgradeOfferRepository(),
                    sharedTimer,
                    callViewCallback,
                    new TimeCounter(),
                    minimizeHandler,
                    chatHeadsController
            );
        } else {
            Logger.d(TAG, "retained call controller");
            retainedCallController.setViewCallback(callViewCallback);
        }
        return retainedCallController;
    }

    public ScreenSharingController getScreenSharingController(ScreenSharingController.ViewCallback gliaScreenSharingCallback) {
        if (gliaScreenSharingCallback != null) {
            if (retainedScreenSharingController == null) {
                Logger.d(TAG, "new screen sharing controller");
                retainedScreenSharingController = new ScreenSharingController(repositoryFactory.getGliaScreenSharingRepository(), gliaScreenSharingCallback);
            } else {
                Logger.d(TAG, "retained screen sharing controller");
                retainedScreenSharingController.setGliaScreenSharingCallback(gliaScreenSharingCallback);
            }
        }
        return retainedScreenSharingController;
    }

    public ChatHeadsController getChatHeadsController(){
        return chatHeadsController;
    }

    public void destroyControllers() {
        destroyCallController(false);
        destroyScreenSharingController(false);
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

    public void destroyScreenSharingController(boolean retain) {
        Logger.d(TAG, "destroyScreenSharingController, retain: " + retain);
        if (retainedScreenSharingController != null) {
            retainedScreenSharingController.onDestroy(retain);
        }
        if (!retain) {
            retainedScreenSharingController = null;
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
