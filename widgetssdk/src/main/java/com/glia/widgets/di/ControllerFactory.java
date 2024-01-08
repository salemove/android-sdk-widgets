package com.glia.widgets.di;

import androidx.annotation.NonNull;

import com.glia.widgets.call.CallController;
import com.glia.widgets.call.CallControllerImpl;
import com.glia.widgets.call.CallViewCallback;
import com.glia.widgets.callvisualizer.ActivityWatcherForCallVisualizerContract;
import com.glia.widgets.callvisualizer.ActivityWatcherForCallVisualizerController;
import com.glia.widgets.callvisualizer.EndScreenSharingContract;
import com.glia.widgets.callvisualizer.EndScreenSharingController;
import com.glia.widgets.callvisualizer.VisitorCodeContract;
import com.glia.widgets.callvisualizer.controller.CallVisualizerController;
import com.glia.widgets.callvisualizer.controller.CallVisualizerControllerImpl;
import com.glia.widgets.callvisualizer.controller.VisitorCodeController;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.chat.controller.ChatController;
import com.glia.widgets.chat.controller.ChatControllerImpl;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.core.screensharing.ScreenSharingControllerImpl;
import com.glia.widgets.engagement.completion.EngagementCompletionController;
import com.glia.widgets.engagement.completion.EngagementCompletionControllerImpl;
import com.glia.widgets.filepreview.ui.FilePreviewContract;
import com.glia.widgets.filepreview.ui.FilePreviewController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.messagecenter.MessageCenterContract;
import com.glia.widgets.messagecenter.MessageCenterController;
import com.glia.widgets.permissions.PermissionsRequestContract;
import com.glia.widgets.permissions.controller.PermissionsRequestController;
import com.glia.widgets.survey.SurveyContract;
import com.glia.widgets.survey.SurveyController;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract;
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoController;
import com.glia.widgets.view.head.ChatHeadContract;
import com.glia.widgets.view.head.ChatHeadLayoutContract;
import com.glia.widgets.view.head.ChatHeadPosition;
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract;
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadController;
import com.glia.widgets.view.head.controller.ApplicationChatHeadLayoutController;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;
import com.glia.widgets.view.snackbar.ActivityWatcherForLiveObservationContract;
import com.glia.widgets.view.snackbar.ActivityWatcherForLiveObservationController;

public class ControllerFactory {

    private static final String TAG = "ControllerFactory";
    private static ChatHeadContract.Controller serviceChatHeadController;
    private static ApplicationChatHeadLayoutController applicationChatHeadController;
    private final RepositoryFactory repositoryFactory;
    private final TimeCounter sharedTimer = new TimeCounter();
    private final MinimizeHandler minimizeHandler = new MinimizeHandler();
    private final DialogController dialogController;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final UseCaseFactory useCaseFactory;
    private final GliaSdkConfigurationManager sdkConfigurationManager;
    private final FilePreviewContract.Controller filePreviewController;
    private final ManagerFactory managerFactory;
    private EngagementCompletionController engagementCompletionController;
    private ChatController retainedChatController;
    private CallController retainedCallController;
    private ScreenSharingController retainedScreenSharingController;
    private SurveyController surveyController;
    private CallVisualizerController callVisualizerController;
    private ActivityWatcherForCallVisualizerController activityWatcherforCallVisualizerController;
    private ActivityWatcherForChatHeadController activityWatcherForChatHeadController;
    private ActivityWatcherForLiveObservationController activityWatcherForLiveObservationController;

    public ControllerFactory(
        RepositoryFactory repositoryFactory,
        UseCaseFactory useCaseFactory,
        GliaSdkConfigurationManager sdkConfigurationManager,
        ManagerFactory managerFactory
    ) {
        this.repositoryFactory = repositoryFactory;
        messagesNotSeenHandler = new MessagesNotSeenHandler(
            useCaseFactory.createGliaOnMessageUseCase()
        );

        this.useCaseFactory = useCaseFactory;
        this.dialogController = new DialogController(
            useCaseFactory.createSetOverlayPermissionRequestDialogShownUseCase(),
            useCaseFactory.createSetEnableCallNotificationChannelDialogShownUseCase()
        );
        this.filePreviewController = new FilePreviewController(
            useCaseFactory.createGetImageFileFromDownloadsUseCase(),
            useCaseFactory.createGetImageFileFromCacheUseCase(),
            useCaseFactory.createPutImageFileToDownloadsUseCase()
        );
        this.sdkConfigurationManager = sdkConfigurationManager;
        this.managerFactory = managerFactory;
    }

    public ChatController getChatController(ChatViewCallback chatViewCallback) {
        if (retainedChatController == null) {
            Logger.d(TAG, "new for chat activity");
            retainedChatController = new ChatControllerImpl(
                chatViewCallback,
                sharedTimer,
                minimizeHandler,
                dialogController,
                messagesNotSeenHandler,
                useCaseFactory.createCallNotificationUseCase(),
                useCaseFactory.operatorTypingUseCase(),
                useCaseFactory.createGliaSendMessagePreviewUseCase(),
                useCaseFactory.createGliaSendMessageUseCase(),
                useCaseFactory.getEndEngagementUseCase(),
                useCaseFactory.createAddFileToAttachmentAndUploadUseCase(),
                useCaseFactory.createAddFileAttachmentsObserverUseCase(),
                useCaseFactory.createRemoveFileAttachmentObserverUseCase(),
                useCaseFactory.createGetFileAttachmentsUseCase(),
                useCaseFactory.createRemoveFileAttachmentUseCase(),
                useCaseFactory.createSupportedFileCountCheckUseCase(),
                useCaseFactory.createIsShowSendButtonUseCase(),
                useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                useCaseFactory.createDownloadFileUseCase(),
                useCaseFactory.createSiteInfoUseCase(),
                useCaseFactory.createIsFromCallScreenUseCase(),
                useCaseFactory.createUpdateFromCallScreenUseCase(),
                useCaseFactory.createIsSecureEngagementUseCase(),
                useCaseFactory.createSetEngagementConfigUseCase(),
                useCaseFactory.createIsSecureConversationsChatAvailableUseCase(),
                useCaseFactory.getIsCurrentEngagementCallVisualizer(),
                useCaseFactory.createIsFileReadyForPreviewUseCase(),
                useCaseFactory.createDetermineGvaButtonTypeUseCase(),
                useCaseFactory.createIsAuthenticatedUseCase(),
                useCaseFactory.createUpdateOperatorDefaultImageUrlUseCase(),
                useCaseFactory.createConfirmationDialogUseCase(),
                useCaseFactory.createConfirmationDialogLinksUseCase(),
                managerFactory.getChatManager(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.getOperatorMediaUseCase(),
                useCaseFactory.getMediaUpgradeOfferUseCase(),
                useCaseFactory.getAcceptMediaUpgradeOfferUseCase(),
                useCaseFactory.getDeclineMediaUpgradeOfferUseCase(),
                useCaseFactory.getIsQueueingOrEngagementUseCase(),
                useCaseFactory.getQueueForEngagementUseCase()
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
            retainedCallController = new CallControllerImpl(
                sdkConfigurationManager,
                sharedTimer,
                callViewCallback,
                new TimeCounter(),
                new TimeCounter(),
                minimizeHandler,
                dialogController,
                messagesNotSeenHandler,
                useCaseFactory.createCallNotificationUseCase(),
                useCaseFactory.getEndEngagementUseCase(),
                useCaseFactory.createShouldShowMediaEngagementViewUseCase(),
                useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                useCaseFactory.createHasCallNotificationChannelEnabledUseCase(),
                useCaseFactory.createIsShowEnableCallNotificationChannelDialogUseCase(),
                useCaseFactory.createUpdateFromCallScreenUseCase(),
                useCaseFactory.getIsCurrentEngagementCallVisualizer(),
                useCaseFactory.createTurnSpeakerphoneUseCase(),
                useCaseFactory.createConfirmationDialogUseCase(),
                useCaseFactory.createConfirmationDialogLinksUseCase(),
                useCaseFactory.createHandleCallPermissionsUseCase(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.getOperatorMediaUseCase(),
                useCaseFactory.getMediaUpgradeOfferUseCase(),
                useCaseFactory.getAcceptMediaUpgradeOfferUseCase(),
                useCaseFactory.getDeclineMediaUpgradeOfferUseCase(),
                useCaseFactory.getVisitorMediaUseCase(),
                useCaseFactory.getToggleVisitorAudioMediaStateUseCase(),
                useCaseFactory.getToggleVisitorVideoMediaStateUseCase(),
                useCaseFactory.getIsQueueingOrEngagementUseCase(),
                useCaseFactory.getQueueForEngagementUseCase()
            );
        } else {
            Logger.d(TAG, "retained call controller");
            retainedCallController.setViewCallback(callViewCallback);
        }
        return retainedCallController;
    }

    public ScreenSharingController getScreenSharingController() {
        if (retainedScreenSharingController == null) {
            Logger.d(TAG, "new screen sharing controller");
            retainedScreenSharingController = new ScreenSharingControllerImpl(
                useCaseFactory.getScreenSharingUseCase(),
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
        destroyChatController();
        serviceChatHeadController.onDestroy();
        applicationChatHeadController.onDestroy();
        messagesNotSeenHandler.onDestroy();
    }

    public void destroyCallController() {
        Logger.d(TAG, "destroyCallController");
        if (retainedCallController != null) {
            retainedCallController.onDestroy(false);
            retainedCallController = null;
        }
    }

    public void destroyChatController() {
        Logger.d(TAG, "destroyChatController");
        if (retainedChatController != null) {
            retainedChatController.onDestroy(false);
            retainedChatController = null;
        }
    }

    public DialogController getDialogController() {
        return dialogController;
    }

    public DialogController createDialogController() {
        return new DialogController(
            useCaseFactory.createSetOverlayPermissionRequestDialogShownUseCase(),
            useCaseFactory.createSetEnableCallNotificationChannelDialogShownUseCase()
        );
    }

    public void init() {
        messagesNotSeenHandler.init();
        getActivityWatcherForChatHeadController().init();
    }

    public FilePreviewContract.Controller getImagePreviewController() {
        return filePreviewController;
    }

    public ChatHeadContract.Controller getChatHeadController() {
        if (serviceChatHeadController == null) {
            serviceChatHeadController = new ServiceChatHeadController(
                useCaseFactory.getToggleChatHeadServiceUseCase(),
                useCaseFactory.getResolveChatHeadNavigationUseCase(),
                messagesNotSeenHandler,
                new ChatHeadPosition(),
                useCaseFactory.createIsCallVisualizerScreenSharingUseCase(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.getCurrentOperatorUseCase(),
                useCaseFactory.getVisitorMediaUseCase()
            );
        }
        return serviceChatHeadController;
    }

    public ChatHeadLayoutContract.Controller getChatHeadLayoutController() {
        if (applicationChatHeadController == null) {
            applicationChatHeadController = new ApplicationChatHeadLayoutController(
                useCaseFactory.getIsDisplayApplicationChatHeadUseCase(),
                useCaseFactory.getResolveChatHeadNavigationUseCase(),
                messagesNotSeenHandler,
                useCaseFactory.createIsCallVisualizerScreenSharingUseCase(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.getCurrentOperatorUseCase(),
                useCaseFactory.getVisitorMediaUseCase()
            );
        }
        return applicationChatHeadController;
    }

    public SurveyContract.Controller getSurveyController() {
        if (surveyController == null) {
            surveyController = new SurveyController(
                useCaseFactory.getSurveyAnswerUseCase()
            );
        }
        return surveyController;
    }

    public CallVisualizerController getCallVisualizerController() {
        if (callVisualizerController == null) {
            callVisualizerController = new CallVisualizerControllerImpl(
                dialogController,
                useCaseFactory.createConfirmationDialogUseCase(),
                useCaseFactory.createIsCallOrChatScreenActiveUseCase(),
                useCaseFactory.getMediaUpgradeOfferUseCase(),
                useCaseFactory.getAcceptMediaUpgradeOfferUseCase(),
                useCaseFactory.getDeclineMediaUpgradeOfferUseCase(),
                useCaseFactory.getEngagementRequestUseCase(),
                useCaseFactory.getCurrentOperatorUseCase(),
                useCaseFactory.getEngagementStateUseCase()
            );
        }
        return callVisualizerController;
    }

    public FloatingVisitorVideoContract.Controller getFloatingVisitorVideoController() {
        return new FloatingVisitorVideoController(
            useCaseFactory.createIsShowVideoUseCase(),
            useCaseFactory.createIsShowOnHoldUseCase(),
            useCaseFactory.getVisitorMediaUseCase());
    }

    public MessageCenterContract.Controller getMessageCenterController(String queueId) {
        return new MessageCenterController(
            serviceChatHeadController,
            useCaseFactory.createSendSecureMessageUseCase(queueId),
            useCaseFactory.createIsMessageCenterAvailableUseCase(queueId),
            useCaseFactory.createAddSecureFileAttachmentsObserverUseCase(),
            useCaseFactory.createAddSecureFileToAttachmentAndUploadUseCase(),
            useCaseFactory.createGetSecureFileAttachmentsUseCase(),
            useCaseFactory.createRemoveSecureFileAttachmentUseCase(),
            useCaseFactory.createIsAuthenticatedUseCase(),
            useCaseFactory.createSiteInfoUseCase(),
            useCaseFactory.createOnNextMessageUseCase(),
            useCaseFactory.createEnableSendMessageButtonUseCase(),
            useCaseFactory.createShowMessageLimitErrorUseCase(),
            useCaseFactory.createResetMessageCenterUseCase(),
            createDialogController()
        );
    }

    public EndScreenSharingContract.Controller getEndScreenSharingController() {
        return new EndScreenSharingController();
    }

    public VisitorCodeContract.Controller getVisitorCodeController() {
        return new VisitorCodeController(
            dialogController,
            repositoryFactory.getVisitorCodeRepository(),
            useCaseFactory.getEngagementStateUseCase(),
            useCaseFactory.getIsQueueingOrEngagementUseCase()
        );
    }

    public ActivityWatcherForCallVisualizerContract.Controller getActivityWatcherForCallVisualizerController() {
        if (activityWatcherforCallVisualizerController == null) {
            activityWatcherforCallVisualizerController = new ActivityWatcherForCallVisualizerController(
                getCallVisualizerController(),
                getScreenSharingController(),
                useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                useCaseFactory.createConfirmationDialogLinksUseCase()
            );
        }
        return activityWatcherforCallVisualizerController;
    }

    public ActivityWatcherForChatHeadContract.Controller getActivityWatcherForChatHeadController() {
        if (activityWatcherForChatHeadController == null) {
            activityWatcherForChatHeadController = new ActivityWatcherForChatHeadController(
                serviceChatHeadController,
                getChatHeadLayoutController(),
                getScreenSharingController(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.createIsFromCallScreenUseCase(),
                useCaseFactory.createUpdateFromCallScreenUseCase(),
                useCaseFactory.getIsCurrentEngagementCallVisualizer());
        }
        return activityWatcherForChatHeadController;
    }

    public PermissionsRequestContract.Controller getPermissionsController() {
        return new PermissionsRequestController(
            repositoryFactory.getPermissionsRequestRepository()
        );
    }

    public ActivityWatcherForLiveObservationContract.Controller getActivityWatcherForLiveObservationController() {
        if (activityWatcherForLiveObservationController == null) {
            activityWatcherForLiveObservationController = new ActivityWatcherForLiveObservationController(
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.createLiveObservationPopupUseCase()
            );
        }
        return activityWatcherForLiveObservationController;
    }

    @NonNull
    public EngagementCompletionController getEndEngagementController() {
        if (engagementCompletionController == null) {
            engagementCompletionController = new EngagementCompletionControllerImpl(
                useCaseFactory.getEngagementCompletionUseCase(),
                useCaseFactory.getReleaseResourcesUseCase(dialogController)
            );
        }
        return engagementCompletionController;
    }
}
