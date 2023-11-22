package com.glia.widgets.di;

import androidx.annotation.NonNull;

import com.glia.widgets.call.CallController;
import com.glia.widgets.call.CallViewCallback;
import com.glia.widgets.callvisualizer.ActivityWatcherForCallVisualizerContract;
import com.glia.widgets.callvisualizer.ActivityWatcherForCallVisualizerController;
import com.glia.widgets.callvisualizer.EndScreenSharingContract;
import com.glia.widgets.callvisualizer.EndScreenSharingController;
import com.glia.widgets.callvisualizer.VisitorCodeContract;
import com.glia.widgets.callvisualizer.controller.CallVisualizerController;
import com.glia.widgets.callvisualizer.controller.VisitorCodeController;
import com.glia.widgets.chat.ChatViewCallback;
import com.glia.widgets.chat.controller.ChatController;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.screensharing.ScreenSharingController;
import com.glia.widgets.engagement.completion.EngagementCompletionController;
import com.glia.widgets.engagement.completion.EngagementCompletionControllerImpl;
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
import com.glia.widgets.view.head.ChatHeadPosition;
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract;
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadController;
import com.glia.widgets.view.head.controller.ApplicationChatHeadLayoutController;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;
import com.glia.widgets.view.snackbar.ActivityWatcherForLiveObservationContract;
import com.glia.widgets.view.snackbar.ActivityWatcherForLiveObservationController;

public class ControllerFactory {

    private static final String TAG = "ControllerFactory";
    private static ServiceChatHeadController serviceChatHeadController;
    private static ApplicationChatHeadLayoutController applicationChatHeadController;
    private final RepositoryFactory repositoryFactory;
    private final TimeCounter sharedTimer = new TimeCounter();
    private final MinimizeHandler minimizeHandler = new MinimizeHandler();
    private final DialogController dialogController;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final UseCaseFactory useCaseFactory;
    private final GliaSdkConfigurationManager sdkConfigurationManager;
    private final FilePreviewController filePreviewController;
    private final ChatHeadPosition chatHeadPosition;
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
        this.chatHeadPosition = ChatHeadPosition.getInstance();
        this.sdkConfigurationManager = sdkConfigurationManager;
        this.managerFactory = managerFactory;
    }

    public ChatController getChatController(ChatViewCallback chatViewCallback) {
        if (retainedChatController == null) {
            Logger.d(TAG, "new for chat activity");
            retainedChatController = new ChatController(
                chatViewCallback,
                sharedTimer,
                minimizeHandler,
                dialogController,
                messagesNotSeenHandler,
                useCaseFactory.createCallNotificationUseCase(),
                useCaseFactory.createQueueForChatEngagementUseCase(),
                useCaseFactory.operatorTypingUseCase(),
                useCaseFactory.createGliaSendMessagePreviewUseCase(),
                useCaseFactory.createGliaSendMessageUseCase(),
                useCaseFactory.createCancelQueueTicketUseCase(),
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
                useCaseFactory.createQueueTicketStateChangeToUnstaffedUseCase(),
                useCaseFactory.createIsQueueingEngagementUseCase(),
                useCaseFactory.createIsSecureEngagementUseCase(),
                useCaseFactory.getHasOngoingEngagementUseCase(),
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
                useCaseFactory.getDeclineMediaUpgradeOfferUseCase()
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
                sdkConfigurationManager,
                sharedTimer,
                callViewCallback,
                new TimeCounter(),
                new TimeCounter(),
                minimizeHandler,
                dialogController,
                messagesNotSeenHandler,
                useCaseFactory.createCallNotificationUseCase(),
                useCaseFactory.createQueueForMediaEngagementUseCase(),
                useCaseFactory.createCancelQueueTicketUseCase(),
                useCaseFactory.getEndEngagementUseCase(),
                useCaseFactory.createShouldShowMediaEngagementViewUseCase(),
                useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                useCaseFactory.createHasCallNotificationChannelEnabledUseCase(),
                useCaseFactory.createIsShowEnableCallNotificationChannelDialogUseCase(),
                useCaseFactory.createUpdateFromCallScreenUseCase(),
                useCaseFactory.createQueueTicketStateChangeToUnstaffedUseCase(),
                useCaseFactory.getIsCurrentEngagementCallVisualizer(),
                useCaseFactory.getHasOngoingEngagementUseCase(),
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
                useCaseFactory.getToggleVisitorVideoMediaStateUseCase()
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
        getCallVisualizerController().init();
        getScreenSharingController().init();
        getActivityWatcherForChatHeadController().init();
    }

    public FilePreviewController getImagePreviewController() {
        return filePreviewController;
    }

    public ServiceChatHeadController getChatHeadController() {
        if (serviceChatHeadController == null) {
            serviceChatHeadController = new ServiceChatHeadController(
                useCaseFactory.getToggleChatHeadServiceUseCase(),
                useCaseFactory.getResolveChatHeadNavigationUseCase(),
                messagesNotSeenHandler,
                chatHeadPosition,
                useCaseFactory.createIsCallVisualizerScreenSharingUseCase(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.getCurrentOperatorUseCase(),
                useCaseFactory.getVisitorMediaUseCase()
            );
        }
        return serviceChatHeadController;
    }

    public ApplicationChatHeadLayoutController getChatHeadLayoutController() {
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
            callVisualizerController = new CallVisualizerController(
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
            useCaseFactory.getHasOngoingEngagementUseCase()
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
                useCaseFactory.getSurveyUseCase(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.getReleaseResourcesUseCase()
            );
        }
        return engagementCompletionController;
    }
}
