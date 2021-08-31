package com.glia.widgets.di;

import android.app.Activity;

import com.glia.widgets.call.CallController;
import com.glia.widgets.call.CallViewCallback;
import com.glia.widgets.chat.controller.ChatController;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.filepreview.ui.FilePreviewController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;
import com.glia.widgets.view.head.ChatHeadsController;

public class ControllerFactory {

    private final RepositoryFactory repositoryFactory;
    private final TimeCounter sharedTimer = new TimeCounter();
    private final MinimizeHandler minimizeHandler = new MinimizeHandler();
    private final ChatHeadsController chatHeadsController;
    private final DialogController dialogController;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final UseCaseFactory useCaseFactory;

    private static final String TAG = "ControllerFactory";

    private ChatController retainedChatController;
    private CallController retainedCallController;
    private ScreenSharingController retainedScreenSharingController;
    private final FilePreviewController filePreviewController;


    public ControllerFactory(RepositoryFactory repositoryFactory, UseCaseFactory useCaseFactory) {
        this.repositoryFactory = repositoryFactory;
        messagesNotSeenHandler = new MessagesNotSeenHandler(
                useCaseFactory.createGliaOnMessageUseCase(),
                useCaseFactory.createOnEngagementEndUseCase()
        );
        chatHeadsController = new ChatHeadsController(
                useCaseFactory.createOnEngagementUseCase(),
                useCaseFactory.createOnEngagementEndUseCase(),
                useCaseFactory.createAddOperatorMediaStateListenerUseCase(),
                useCaseFactory.createAddQueueTicketsEventsListenerUseCase(),
                messagesNotSeenHandler,
                useCaseFactory.createGetIsMediaEngagementOngoingUseCase(),
                useCaseFactory.createHasOverlayEnabledUseCase()
        );

        this.useCaseFactory = useCaseFactory;
        this.dialogController = new DialogController(
                useCaseFactory.createSetOverlayPermissionRequestDialogShownUseCase(),
                useCaseFactory.createSetEnableCallNotificationChannelDialogShownUseCase()
        );
        this.filePreviewController = new FilePreviewController(
                useCaseFactory.createGetImageFileFromDownloadsUseCase(),
                useCaseFactory.createGetImageFileFromCacheUseCase(),
                useCaseFactory.createGetImageFileFromNetworkUseCase(),
                useCaseFactory.createPutImageFileToDownloadsUseCase(),
                useCaseFactory.createOnEngagementEndUseCase()
        );
    }

    public ChatController getChatController(Activity activity, ChatViewCallback chatViewCallback) {
        if (retainedChatController == null) {
            Logger.d(TAG, "new for chat activity");
            retainedChatController = new ChatController(
                    repositoryFactory.getMediaUpgradeOfferRepository(),
                    sharedTimer,
                    chatViewCallback,
                    minimizeHandler,
                    dialogController,
                    messagesNotSeenHandler,
                    useCaseFactory.createShowAudioCallNotificationUseCase(),
                    useCaseFactory.createShowVideoCallNotificationUseCase(),
                    useCaseFactory.createRemoveCallNotificationUseCase(),
                    useCaseFactory.createGliaLoadHistoryUseCase(),
                    useCaseFactory.createQueueForChatEngagementUseCase(),
                    useCaseFactory.createOnEngagementUseCase(),
                    useCaseFactory.createOnEngagementEndUseCase(),
                    useCaseFactory.createGliaOnMessageUseCase(),
                    useCaseFactory.createGliaSendMessagePreviewUseCase(),
                    useCaseFactory.createGliaSendMessageUseCase(),
                    useCaseFactory.createAddOperatorMediaStateListenerUseCase(),
                    useCaseFactory.createCancelQueueTicketUseCase(),
                    useCaseFactory.createGetIsQueueingOngoingUseCase(),
                    useCaseFactory.createEndEngagementUseCase(),
                    useCaseFactory.createOnUpgradeToMediaEngagementUseCase(),
                    useCaseFactory.createAddFileToAttachmentAndUploadUseCase(),
                    useCaseFactory.createAddFileAttachmentsObserverUseCase(),
                    useCaseFactory.createRemoveFileAttachmentObserverUseCase(),
                    useCaseFactory.createGetFileAttachmentsUseCase(),
                    useCaseFactory.createRemoveFileAttachmentUseCase(),
                    useCaseFactory.createIsShowSendButtonUseCase(),
                    useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase()
            );
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
                    repositoryFactory.getMediaUpgradeOfferRepository(),
                    sharedTimer,
                    callViewCallback,
                    new TimeCounter(),
                    new TimeCounter(),
                    minimizeHandler,
                    dialogController,
                    messagesNotSeenHandler,
                    useCaseFactory.createShowAudioCallNotificationUseCase(),
                    useCaseFactory.createShowVideoCallNotificationUseCase(),
                    useCaseFactory.createRemoveCallNotificationUseCase(),
                    useCaseFactory.createQueueForMediaEngagementUseCase(),
                    useCaseFactory.createCancelQueueTicketUseCase(),
                    useCaseFactory.createOnEngagementUseCase(),
                    useCaseFactory.createAddOperatorMediaStateListenerUseCase(),
                    useCaseFactory.createGliaOnVisitorMediaStateUseCase(),
                    useCaseFactory.createOnEngagementEndUseCase(),
                    useCaseFactory.createEndEngagementUseCase(),
                    useCaseFactory.createShouldShowMediaEngagementViewUseCase(),
                    useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                    useCaseFactory.createHasCallNotificationChannelEnabledUseCase(),
                    useCaseFactory.createIsShowEnableCallNotificationChannelDialogUseCase()
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
                retainedScreenSharingController = new ScreenSharingController(
                        repositoryFactory.getGliaScreenSharingRepository(),
                        dialogController,
                        useCaseFactory.createShowScreenSharingNotificationUseCase(),
                        useCaseFactory.createRemoveScreenSharingNotificationUseCase(),
                        useCaseFactory.createHasScreenSharingNotificationChannelEnabledUseCase(),
                        gliaScreenSharingCallback
                );
            } else {
                Logger.d(TAG, "retained screen sharing controller");
                retainedScreenSharingController.setGliaScreenSharingCallback(gliaScreenSharingCallback);
            }
        }
        return retainedScreenSharingController;
    }

    public ChatHeadsController getChatHeadsController() {
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

    public DialogController getDialogController(DialogController.Callback callback) {
        dialogController.addCallback(callback);
        return dialogController;
    }

    public void init() {
        chatHeadsController.initChatObserving();
        messagesNotSeenHandler.init();
    }

    public FilePreviewController getImagePreviewController() {
        return filePreviewController;
    }
}
