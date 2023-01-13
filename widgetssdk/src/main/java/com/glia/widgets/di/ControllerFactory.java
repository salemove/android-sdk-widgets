package com.glia.widgets.di;

import com.glia.widgets.call.CallController;
import com.glia.widgets.call.CallViewCallback;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.chat.controller.ChatController;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.filepreview.ui.FilePreviewController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.messagecenter.MessageCenterContract;
import com.glia.widgets.messagecenter.MessageCenterController;
import com.glia.widgets.survey.SurveyContract;
import com.glia.widgets.survey.SurveyController;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract;
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoController;
import com.glia.widgets.view.head.ChatHeadLayoutContract;
import com.glia.widgets.view.head.ChatHeadPosition;
import com.glia.widgets.view.head.controller.ApplicationChatHeadLayoutController;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;

public class ControllerFactory {

    private final RepositoryFactory repositoryFactory;
    private final TimeCounter sharedTimer = new TimeCounter();
    private final MinimizeHandler minimizeHandler = new MinimizeHandler();
    private final DialogController dialogController;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final UseCaseFactory useCaseFactory;
    private final GliaSdkConfigurationManager sdkConfigurationManager;

    private static final String TAG = "ControllerFactory";

    private ChatController retainedChatController;
    private CallController retainedCallController;
    private ScreenSharingController retainedScreenSharingController;
    private final FilePreviewController filePreviewController;
    private final ChatHeadPosition chatHeadPosition;
    private SurveyController surveyController;

    private static ServiceChatHeadController serviceChatHeadController;

    public ControllerFactory(
            RepositoryFactory repositoryFactory,
            UseCaseFactory useCaseFactory,
            GliaSdkConfigurationManager sdkConfigurationManager
    ) {
        this.repositoryFactory = repositoryFactory;
        messagesNotSeenHandler = new MessagesNotSeenHandler(
                useCaseFactory.createGliaOnMessageUseCase(),
                useCaseFactory.createOnEngagementEndUseCase()
        );

        this.useCaseFactory = useCaseFactory;
        this.dialogController = new DialogController(
                useCaseFactory.createSetOverlayPermissionRequestDialogShownUseCase(),
                useCaseFactory.createSetEnableCallNotificationChannelDialogShownUseCase()
        );
        this.filePreviewController = new FilePreviewController(
                useCaseFactory.createGetImageFileFromDownloadsUseCase(),
                useCaseFactory.createGetImageFileFromCacheUseCase(),
                useCaseFactory.createPutImageFileToDownloadsUseCase(),
                useCaseFactory.createOnEngagementEndUseCase()
        );
        this.chatHeadPosition = ChatHeadPosition.getInstance();
        this.sdkConfigurationManager = sdkConfigurationManager;
    }

    public ChatController getChatController(ChatViewCallback chatViewCallback) {
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
                    useCaseFactory.createGliaOnOperatorTypingUseCase(),
                    useCaseFactory.createGliaSendMessagePreviewUseCase(),
                    useCaseFactory.createGliaSendMessageUseCase(),
                    useCaseFactory.createAddOperatorMediaStateListenerUseCase(),
                    useCaseFactory.createCancelQueueTicketUseCase(),
                    useCaseFactory.createEndEngagementUseCase(),
                    useCaseFactory.createAddFileToAttachmentAndUploadUseCase(),
                    useCaseFactory.createAddFileAttachmentsObserverUseCase(),
                    useCaseFactory.createRemoveFileAttachmentObserverUseCase(),
                    useCaseFactory.createGetFileAttachmentsUseCase(),
                    useCaseFactory.createRemoveFileAttachmentUseCase(),
                    useCaseFactory.createSupportedFileCountCheckUseCase(),
                    useCaseFactory.createIsShowSendButtonUseCase(),
                    useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                    useCaseFactory.createDownloadFileUseCase(),
                    useCaseFactory.createIsEnableChatEditTextUseCase(),
                    useCaseFactory.createSiteInfoUseCase(),
                    useCaseFactory.getGliaSurveyUseCase(),
                    useCaseFactory.createGetGliaEngagementStateFlowableUseCase(),
                    useCaseFactory.createIsFromCallScreenUseCase(),
                    useCaseFactory.createUpdateFromCallScreenUseCase(),
                    useCaseFactory.createCustomCardAdapterTypeUseCase(),
                    useCaseFactory.createCustomCardTypeUseCase(),
                    useCaseFactory.createCustomCardInteractableUseCase(),
                    useCaseFactory.createCustomCardShouldShowUseCase(),
                    useCaseFactory.createQueueTicketStateChangeToUnstaffedUseCase());
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
                    useCaseFactory.createOnEngagementEndUseCase(),
                    useCaseFactory.createEndEngagementUseCase(),
                    useCaseFactory.createShouldShowMediaEngagementViewUseCase(),
                    useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                    useCaseFactory.createHasCallNotificationChannelEnabledUseCase(),
                    useCaseFactory.createIsShowEnableCallNotificationChannelDialogUseCase(),
                    useCaseFactory.getGliaSurveyUseCase(),
                    useCaseFactory.createAddVisitorMediaStateListenerUseCase(),
                    useCaseFactory.createRemoveVisitorMediaStateListenerUseCase(),
                    useCaseFactory.createToggleVisitorAudioMediaMuteUseCase(),
                    useCaseFactory.createToggleVisitorVideoUseCase(),
                    useCaseFactory.createGetGliaEngagementStateFlowableUseCase(),
                    useCaseFactory.createUpdateFromCallScreenUseCase(),
                    useCaseFactory.createQueueTicketStateChangeToUnstaffedUseCase());
        } else {
            Logger.d(TAG, "retained call controller");
            retainedCallController.setViewCallback(callViewCallback);
        }
        return retainedCallController;
    }

    public ScreenSharingController getScreenSharingController() {
        if (retainedScreenSharingController == null) {
            Logger.d(TAG, "new screen sharing controller");
            retainedScreenSharingController = new ScreenSharingController(
                    repositoryFactory.getGliaScreenSharingRepository(),
                    dialogController,
                    useCaseFactory.createShowScreenSharingNotificationUseCase(),
                    useCaseFactory.createRemoveScreenSharingNotificationUseCase(),
                    useCaseFactory.createHasScreenSharingNotificationChannelEnabledUseCase(),
                    sdkConfigurationManager
            );
        }
        return retainedScreenSharingController;
    }

    public void destroyControllers() {
        destroyCallController();
        destroyScreenSharingController();
        destroyChatController();
        serviceChatHeadController.onDestroy();
    }

    public void destroyCallController() {
        Logger.d(TAG, "destroyCallController");
        if (retainedCallController != null) {
            retainedCallController.onDestroy(false);
            retainedCallController = null;
        }
    }

    public void destroyScreenSharingController() {
        Logger.d(TAG, "destroyScreenSharingController");
        if (retainedScreenSharingController != null) {
            retainedScreenSharingController.onDestroy();
            retainedScreenSharingController = null;
        }
    }

    private void destroyChatController() {
        Logger.d(TAG, "destroyChatController");
        if (retainedChatController != null) {
            retainedChatController.onDestroy(false);
            retainedChatController = null;
        }
    }

    public DialogController getDialogController() {
        return dialogController;
    }

    public void init() {
        messagesNotSeenHandler.init();
    }

    public FilePreviewController getImagePreviewController() {
        return filePreviewController;
    }

    public ServiceChatHeadController getChatHeadController() {
        if (serviceChatHeadController == null) {
            serviceChatHeadController = new ServiceChatHeadController(
                    useCaseFactory.getToggleChatHeadServiceUseCase(),
                    useCaseFactory.getResolveChatHeadNavigationUseCase(),
                    useCaseFactory.createOnEngagementUseCase(),
                    useCaseFactory.createOnEngagementEndUseCase(),
                    messagesNotSeenHandler,
                    useCaseFactory.createAddVisitorMediaStateListenerUseCase(),
                    useCaseFactory.createRemoveVisitorMediaStateListenerUseCase(),
                    chatHeadPosition,
                    useCaseFactory.createGetOperatorFlowableUseCase()
            );
        }
        return serviceChatHeadController;
    }

    public ChatHeadLayoutContract.Controller getChatHeadLayoutController() {
        return new ApplicationChatHeadLayoutController(
                useCaseFactory.getIsDisplayApplicationChatHeadUseCase(),
                useCaseFactory.getResolveChatHeadNavigationUseCase(),
                useCaseFactory.createOnEngagementUseCase(),
                useCaseFactory.createOnEngagementEndUseCase(),
                messagesNotSeenHandler,
                useCaseFactory.createAddVisitorMediaStateListenerUseCase(),
                useCaseFactory.createRemoveVisitorMediaStateListenerUseCase(),
                useCaseFactory.createGetOperatorFlowableUseCase()
        );
    }

    public SurveyContract.Controller getSurveyController() {
        if (surveyController == null) {
            surveyController = new SurveyController(
                    useCaseFactory.getSurveyAnswerUseCase()
            );
        }
        return surveyController;
    }

    public FloatingVisitorVideoContract.Controller getFloatingVisitorVideoController() {
        return new FloatingVisitorVideoController(
                useCaseFactory.createAddVisitorMediaStateListenerUseCase(),
                useCaseFactory.createRemoveVisitorMediaStateListenerUseCase(),
                useCaseFactory.createIsShowVideoUseCase(),
                useCaseFactory.createIsShowOnHoldUseCase()
        );
    }

    public MessageCenterContract.Controller getMessageCenterController(String queueId) {
        return new MessageCenterController(
                useCaseFactory.createSendSecureMessageUseCase(queueId),
                useCaseFactory.createIsMessageCenterAvailableUseCase(queueId),
                useCaseFactory.createAddSecureFileAttachmentsObserverUseCase(),
                useCaseFactory.createAddSecureFileToAttachmentAndUploadUseCase(),
                useCaseFactory.createGetSecureFileAttachmentsUseCase(),
                useCaseFactory.createRemoveSecureFileAttachmentObserverUseCase(),
                useCaseFactory.createRemoveSecureFileAttachmentUseCase()
        );
    }
}
