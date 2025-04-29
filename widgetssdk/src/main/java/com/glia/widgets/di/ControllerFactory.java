package com.glia.widgets.di;

import androidx.annotation.NonNull;

import com.glia.widgets.call.CallContract;
import com.glia.widgets.call.CallController;
import com.glia.widgets.callvisualizer.EndScreenSharingContract;
import com.glia.widgets.callvisualizer.EndScreenSharingController;
import com.glia.widgets.callvisualizer.VisitorCodeContract;
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract;
import com.glia.widgets.callvisualizer.controller.CallVisualizerController;
import com.glia.widgets.callvisualizer.controller.VisitorCodeController;
import com.glia.widgets.chat.ChatContract;
import com.glia.widgets.chat.controller.ChatController;
import com.glia.widgets.core.dialog.DialogContract;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.engagement.completion.EngagementCompletionContract;
import com.glia.widgets.engagement.completion.EngagementCompletionController;
import com.glia.widgets.entrywidget.EntryWidgetContract;
import com.glia.widgets.entrywidget.EntryWidgetController;
import com.glia.widgets.entrywidget.EntryWidgetHideController;
import com.glia.widgets.filepreview.ui.ImagePreviewContract;
import com.glia.widgets.filepreview.ui.ImagePreviewController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.messagecenter.MessageCenterContract;
import com.glia.widgets.messagecenter.MessageCenterController;
import com.glia.widgets.operator.OperatorRequestContract;
import com.glia.widgets.operator.OperatorRequestController;
import com.glia.widgets.permissions.PermissionsRequestContract;
import com.glia.widgets.permissions.controller.PermissionsRequestController;
import com.glia.widgets.survey.SurveyContract;
import com.glia.widgets.survey.SurveyController;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;
import com.glia.widgets.view.head.ChatHeadContract;
import com.glia.widgets.view.head.ChatHeadLayoutContract;
import com.glia.widgets.view.head.ChatHeadPosition;
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadContract;
import com.glia.widgets.view.head.controller.ActivityWatcherForChatHeadController;
import com.glia.widgets.view.head.controller.ApplicationChatHeadLayoutController;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;
import com.glia.widgets.view.snackbar.SnackbarContract;
import com.glia.widgets.view.snackbar.SnackbarController;
import com.glia.widgets.view.snackbar.liveobservation.ActivityWatcherForLiveObservationContract;
import com.glia.widgets.view.snackbar.liveobservation.ActivityWatcherForLiveObservationController;

/**
 * @hide
 */
public class ControllerFactory {

    private static final String TAG = "ControllerFactory";
    private static ChatHeadContract.Controller serviceChatHeadController;
    private static ChatHeadLayoutContract.Controller applicationChatHeadController;
    private final RepositoryFactory repositoryFactory;
    private final TimeCounter sharedTimer = new TimeCounter();
    private final MinimizeHandler minimizeHandler = new MinimizeHandler();
    private final DialogContract.Controller dialogController;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final UseCaseFactory useCaseFactory;
    private final ImagePreviewContract.Controller filePreviewController;
    private final ManagerFactory managerFactory;
    private final GliaCore core;
    private EngagementCompletionContract.Controller engagementCompletionController;
    private ChatContract.Controller retainedChatController;
    private CallContract.Controller retainedCallController;
    private SurveyController surveyController;
    private CallVisualizerContract.Controller callVisualizerController;
    private ActivityWatcherForChatHeadController activityWatcherForChatHeadController;
    private ActivityWatcherForLiveObservationController activityWatcherForLiveObservationController;
    private EntryWidgetHideController entryWidgetHideController;
    private SnackbarController snackbarController;

    public ControllerFactory(
        RepositoryFactory repositoryFactory,
        UseCaseFactory useCaseFactory,
        ManagerFactory managerFactory,
        GliaCore core
    ) {
        this.repositoryFactory = repositoryFactory;
        messagesNotSeenHandler = new MessagesNotSeenHandler(
            useCaseFactory.createGliaOnMessageUseCase()
        );

        this.useCaseFactory = useCaseFactory;
        this.core = core;
        this.dialogController = new DialogController();
        this.filePreviewController = new ImagePreviewController(
            useCaseFactory.createGetImageFileFromDownloadsUseCase(),
            useCaseFactory.createGetImageFileFromCacheUseCase(),
            useCaseFactory.createPutImageFileToDownloadsUseCase()
        );
        this.managerFactory = managerFactory;
    }

    public ChatContract.Controller getChatController() {
        if (retainedChatController == null) {
            Logger.d(TAG, "new for chat activity");
            retainedChatController = new ChatController(
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
                useCaseFactory.createGetFileAttachmentsUseCase(),
                useCaseFactory.createRemoveFileAttachmentUseCase(),
                useCaseFactory.createSupportedFileCountCheckUseCase(),
                useCaseFactory.createIsShowSendButtonUseCase(),
                useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                useCaseFactory.createDownloadFileUseCase(),
                useCaseFactory.createSiteInfoUseCase(),
                useCaseFactory.createIsFromCallScreenUseCase(),
                useCaseFactory.createUpdateFromCallScreenUseCase(),
                useCaseFactory.createManageSecureMessagingStatusUseCase(),
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
                useCaseFactory.getAcceptMediaUpgradeOfferUseCase(),
                useCaseFactory.getIsQueueingOrEngagementUseCase(),
                useCaseFactory.getQueueForEngagementUseCase(),
                useCaseFactory.getDecideOnQueueingUseCase(),
                useCaseFactory.getScreenSharingUseCase(),
                useCaseFactory.getTakePictureUseCase(),
                useCaseFactory.getUriToFileAttachmentUseCase(),
                useCaseFactory.getWithCameraPermissionUseCase(),
                useCaseFactory.getWithReadWritePermissionsUseCase(),
                useCaseFactory.getRequestNotificationPermissionIfPushNotificationsSetUpUseCase(),
                useCaseFactory.getReleaseResourcesUseCase(getDialogController()),
                useCaseFactory.createGetUrlFromLinkUseCase(),
                useCaseFactory.createIsMessagingAvailableUseCase(),
                useCaseFactory.createSecureConversationTopBannerVisibilityUseCase(),
                useCaseFactory.createSetLeaveSecureConversationDialogVisibleUseCase(),
                useCaseFactory.getSetChatScreenOpenUseCase(),
                useCaseFactory.getHasOngoingSecureConversationUseCase()
            );
        }

        return retainedChatController;
    }

    public CallContract.Controller getCallController() {
        if (retainedCallController == null) {
            Logger.d(TAG, "new call controller");
            retainedCallController = new CallController(
                sharedTimer,
                new TimeCounter(),
                new TimeCounter(),
                minimizeHandler,
                dialogController,
                messagesNotSeenHandler,
                useCaseFactory.createCallNotificationUseCase(),
                useCaseFactory.getEndEngagementUseCase(),
                useCaseFactory.createShouldShowMediaEngagementViewUseCase(),
                useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
                useCaseFactory.createUpdateFromCallScreenUseCase(),
                useCaseFactory.getIsCurrentEngagementCallVisualizer(),
                useCaseFactory.createTurnSpeakerphoneUseCase(),
                useCaseFactory.createConfirmationDialogUseCase(),
                useCaseFactory.createConfirmationDialogLinksUseCase(),
                useCaseFactory.createHandleCallPermissionsUseCase(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.getOperatorMediaUseCase(),
                useCaseFactory.getAcceptMediaUpgradeOfferUseCase(),
                useCaseFactory.getVisitorMediaUseCase(),
                useCaseFactory.getToggleVisitorAudioMediaStateUseCase(),
                useCaseFactory.getToggleVisitorVideoMediaStateUseCase(),
                useCaseFactory.getFlipVisitorCameraUseCase(),
                useCaseFactory.getFlipCameraButtonStateUseCase(),
                useCaseFactory.getIsQueueingOrEngagementUseCase(),
                useCaseFactory.getQueueForEngagementUseCase(),
                useCaseFactory.getDecideOnQueueingUseCase(),
                useCaseFactory.getScreenSharingUseCase(),
                useCaseFactory.createGetUrlFromLinkUseCase()
            );
        }

        return retainedCallController;
    }

    public void destroyControllers() {
        destroyCallController();
        destroyChatController();
        serviceChatHeadController.onDestroy();
        applicationChatHeadController.onDestroy();
        messagesNotSeenHandler.onDestroy();
    }

    public void destroyControllersForAuthentication() {
        destroyCallController();
        destroyChatController();
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

    public DialogContract.Controller getDialogController() {
        return dialogController;
    }

    public DialogContract.Controller createDialogController() {
        return new DialogController();
    }

    public void init() {
        messagesNotSeenHandler.init();
        getActivityWatcherForChatHeadController().init();
    }

    public ImagePreviewContract.Controller getImagePreviewController() {
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
                useCaseFactory.getVisitorMediaUseCase(),
                useCaseFactory.getScreenSharingUseCase(),
                useCaseFactory.getEngagementTypeUseCase()
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
                useCaseFactory.getVisitorMediaUseCase(),
                useCaseFactory.getScreenSharingUseCase(),
                useCaseFactory.getEngagementTypeUseCase()
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

    public CallVisualizerContract.Controller getCallVisualizerController() {
        if (callVisualizerController == null) {
            callVisualizerController = new CallVisualizerController(
                dialogController,
                useCaseFactory.createConfirmationDialogUseCase(),
                useCaseFactory.getEngagementRequestUseCase(),
                useCaseFactory.getEngagementStateUseCase(),
                useCaseFactory.createConfirmationDialogLinksUseCase(),
                useCaseFactory.createGetUrlFromLinkUseCase(),
                useCaseFactory.getOnIncomingEngagementRequestTimeoutUseCase()
            );
        }
        return callVisualizerController;
    }

    public MessageCenterContract.Controller getMessageCenterController() {
        return new MessageCenterController(
            useCaseFactory.createSendSecureMessageUseCase(),
            useCaseFactory.createAddFileAttachmentsObserverUseCase(),
            useCaseFactory.createAddSecureFileToAttachmentAndUploadUseCase(),
            useCaseFactory.createGetFileAttachmentsUseCase(),
            useCaseFactory.createRemoveFileAttachmentUseCase(),
            useCaseFactory.createIsAuthenticatedUseCase(),
            useCaseFactory.createSiteInfoUseCase(),
            useCaseFactory.createOnNextMessageUseCase(),
            useCaseFactory.createEnableSendMessageButtonUseCase(),
            useCaseFactory.createShowMessageLimitErrorUseCase(),
            useCaseFactory.createResetMessageCenterUseCase(),
            createDialogController(),
            useCaseFactory.getTakePictureUseCase(),
            useCaseFactory.getUriToFileAttachmentUseCase(),
            useCaseFactory.getRequestNotificationPermissionIfPushNotificationsSetUpUseCase(),
            useCaseFactory.createIsMessagingAvailableUseCase(),
            useCaseFactory.getIsQueueingOrEngagementUseCase()
        );
    }

    public EndScreenSharingContract.Controller getEndScreenSharingController() {
        return new EndScreenSharingController(useCaseFactory.getEndScreenSharingUseCase());
    }

    public VisitorCodeContract.Controller getVisitorCodeController() {
        return new VisitorCodeController(
            callVisualizerController,
            repositoryFactory.getVisitorCodeRepository(),
            useCaseFactory.getEngagementStateUseCase(),
            useCaseFactory.getIsQueueingOrEngagementUseCase()
        );
    }

    public ActivityWatcherForChatHeadContract.Controller getActivityWatcherForChatHeadController() {
        if (activityWatcherForChatHeadController == null) {
            activityWatcherForChatHeadController = new ActivityWatcherForChatHeadController(
                serviceChatHeadController,
                getChatHeadLayoutController(),
                useCaseFactory.getScreenSharingUseCase(),
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
    public EngagementCompletionContract.Controller getEndEngagementController() {
        if (engagementCompletionController == null) {
            engagementCompletionController = new EngagementCompletionController(
                useCaseFactory.getReleaseResourcesUseCase(dialogController),
                useCaseFactory.getEngagementStateUseCase()
            );
        }
        return engagementCompletionController;
    }

    @NonNull
    public OperatorRequestContract.Controller getOperatorRequestController() {
        return new OperatorRequestController(
            useCaseFactory.getOperatorMediaUpgradeOfferUseCase(),
            useCaseFactory.getAcceptMediaUpgradeOfferUseCase(),
            useCaseFactory.getDeclineMediaUpgradeOfferUseCase(),
            useCaseFactory.getCheckMediaUpgradePermissionsUseCase(),
            useCaseFactory.getScreenSharingUseCase(),
            useCaseFactory.getCurrentOperatorUseCase(),
            useCaseFactory.createIsShowOverlayPermissionRequestDialogUseCase(),
            useCaseFactory.getIsCurrentEngagementCallVisualizer(),
            useCaseFactory.createSetOverlayPermissionRequestDialogShownUseCase(),
            dialogController,
            useCaseFactory.getWithNotificationPermissionUseCase(),
            useCaseFactory.getPrepareToScreenSharingUseCase(),
            useCaseFactory.getReleaseScreenSharingResourcesUseCase()
        );
    }

    public EntryWidgetContract.Controller getEntryWidgetController() {
        return new EntryWidgetController(
            repositoryFactory.getQueueRepository(),
            useCaseFactory.createIsAuthenticatedUseCase(),
            repositoryFactory.getSecureConversationsRepository(),
            useCaseFactory.getHasOngoingSecureConversationUseCase(),
            useCaseFactory.getEngagementStateUseCase(),
            useCaseFactory.getEngagementTypeUseCase(),
            core,
            Dependencies.getEngagementLauncher(),
            Dependencies.getActivityLauncher()
        );
    }

    public EntryWidgetHideController getEntryWidgetHideController() {
        if (entryWidgetHideController == null) {
            entryWidgetHideController = new EntryWidgetHideController();
        }
        return entryWidgetHideController;
    }

    public SnackbarContract.Controller getSnackbarController() {
        if (snackbarController == null) {
            snackbarController = new SnackbarController();
        }
        return snackbarController;
    }
}
