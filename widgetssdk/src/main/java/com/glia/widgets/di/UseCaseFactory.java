package com.glia.widgets.di;

import androidx.annotation.NonNull;

import com.glia.androidsdk.visitor.Authentication;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.call.domain.ToggleVisitorAudioMediaMuteUseCase;
import com.glia.widgets.call.domain.ToggleVisitorVideoUseCase;
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase;
import com.glia.widgets.chat.domain.AddNewMessagesDividerUseCase;
import com.glia.widgets.chat.domain.CustomCardAdapterTypeUseCase;
import com.glia.widgets.chat.domain.CustomCardInteractableUseCase;
import com.glia.widgets.chat.domain.CustomCardShouldShowUseCase;
import com.glia.widgets.chat.domain.CustomCardTypeUseCase;
import com.glia.widgets.chat.domain.DecodeSampledBitmapFromInputStreamUseCase;
import com.glia.widgets.chat.domain.FindNewMessagesDividerIndexUseCase;
import com.glia.widgets.chat.domain.GliaLoadHistoryUseCase;
import com.glia.widgets.chat.domain.GliaOnMessageUseCase;
import com.glia.widgets.chat.domain.GliaOnOperatorTypingUseCase;
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase;
import com.glia.widgets.chat.domain.GliaSendMessageUseCase;
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase;
import com.glia.widgets.chat.domain.IsEnableChatEditTextUseCase;
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase;
import com.glia.widgets.chat.domain.IsSecureConversationsChatAvailableUseCase;
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase;
import com.glia.widgets.chat.domain.SiteInfoUseCase;
import com.glia.widgets.chat.domain.UnengagementMessageUseCase;
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase;
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerEndUseCase;
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerUseCase;
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase;
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase;
import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.chathead.SurveyStateManager;
import com.glia.widgets.core.chathead.domain.HasPendingSurveyUseCase;
import com.glia.widgets.core.chathead.domain.IsDisplayApplicationChatHeadUseCase;
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase;
import com.glia.widgets.core.chathead.domain.SetPendingSurveyUseCase;
import com.glia.widgets.core.chathead.domain.SetPendingSurveyUsedUseCase;
import com.glia.widgets.core.chathead.domain.ToggleChatHeadServiceUseCase;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCase;
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase;
import com.glia.widgets.core.engagement.domain.GetEngagementStateFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GetOperatorFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GetOperatorUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.engagement.domain.IsOngoingEngagementUseCase;
import com.glia.widgets.core.engagement.domain.IsQueueingEngagementUseCase;
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase;
import com.glia.widgets.core.engagement.domain.SetEngagementConfigUseCase;
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase;
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase;
import com.glia.widgets.core.mediaupgradeoffer.domain.AddMediaUpgradeOfferCallbackUseCase;
import com.glia.widgets.core.mediaupgradeoffer.domain.RemoveMediaUpgradeOfferCallbackUseCase;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.domain.CallNotificationUseCase;
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.core.permissions.domain.HasCallNotificationChannelEnabledUseCase;
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForMediaEngagementUseCase;
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase;
import com.glia.widgets.core.secureconversations.domain.AddSecureFileAttachmentsObserverUseCase;
import com.glia.widgets.core.secureconversations.domain.AddSecureFileToAttachmentAndUploadUseCase;
import com.glia.widgets.core.secureconversations.domain.GetSecureFileAttachmentsUseCase;
import com.glia.widgets.core.secureconversations.domain.GetUnreadMessagesCountWithTimeoutUseCase;
import com.glia.widgets.core.secureconversations.domain.IsMessageCenterAvailableUseCase;
import com.glia.widgets.core.secureconversations.domain.IsMessagingAvailableUseCase;
import com.glia.widgets.core.secureconversations.domain.IsSecureEngagementUseCase;
import com.glia.widgets.core.secureconversations.domain.MarkMessagesReadWithDelayUseCase;
import com.glia.widgets.core.secureconversations.domain.OnNextMessageUseCase;
import com.glia.widgets.core.secureconversations.domain.RemoveSecureFileAttachmentUseCase;
import com.glia.widgets.core.secureconversations.domain.ResetMessageCenterUseCase;
import com.glia.widgets.core.secureconversations.domain.SendMessageButtonStateUseCase;
import com.glia.widgets.core.secureconversations.domain.SendSecureMessageUseCase;
import com.glia.widgets.core.secureconversations.domain.ShowMessageLimitErrorUseCase;
import com.glia.widgets.core.survey.domain.GliaSurveyAnswerUseCase;
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase;
import com.glia.widgets.helper.rx.Schedulers;
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowOnHoldUseCase;
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowVideoUseCase;

public class UseCaseFactory {
    private static CallNotificationUseCase callNotificationUseCase;
    private static ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private static RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private static ToggleChatHeadServiceUseCase toggleChatHeadServiceUseCase;
    private static IsDisplayApplicationChatHeadUseCase isDisplayApplicationChatHeadUseCase;
    private static ResolveChatHeadNavigationUseCase resolveChatHeadNavigationUseCase;
    private static VisitorCodeViewBuilderUseCase visitorCodeViewBuilderUseCase;

    private static GliaQueueForChatEngagementUseCase gliaQueueForChatEngagementUseCase;
    private static GliaQueueForMediaEngagementUseCase gliaQueueForMediaEngagementUseCase;
    private final RepositoryFactory repositoryFactory;
    private final PermissionManager permissionManager;
    private final PermissionDialogManager permissionDialogManager;
    private final GliaSdkConfigurationManager gliaSdkConfigurationManager;
    private static final SurveyStateManager surveyStateManager = new SurveyStateManager();
    private final INotificationManager notificationManager;
    private final ChatHeadManager chatHeadManager;
    private final Schedulers schedulers;
    private final GliaCore gliaCore;

    public UseCaseFactory(RepositoryFactory repositoryFactory,
                          PermissionManager permissionManager,
                          PermissionDialogManager permissionDialogManager,
                          INotificationManager notificationManager,
                          GliaSdkConfigurationManager gliaSdkConfigurationManager,
                          ChatHeadManager chatHeadManager,
                          Schedulers schedulers,
                          GliaCore gliaCore) {
        this.repositoryFactory = repositoryFactory;
        this.permissionManager = permissionManager;
        this.permissionDialogManager = permissionDialogManager;
        this.notificationManager = notificationManager;
        this.gliaSdkConfigurationManager = gliaSdkConfigurationManager;
        this.chatHeadManager = chatHeadManager;
        this.schedulers = schedulers;
        this.gliaCore = gliaCore;
    }

    @NonNull
    public ToggleChatHeadServiceUseCase getToggleChatHeadServiceUseCase() {
        if (toggleChatHeadServiceUseCase == null) {
            toggleChatHeadServiceUseCase = new ToggleChatHeadServiceUseCase(
                    repositoryFactory.getGliaEngagementRepository(),
                    repositoryFactory.getGliaQueueRepository(),
                    repositoryFactory.getGliaScreenSharingRepository(),
                    chatHeadManager,
                    permissionManager,
                    gliaSdkConfigurationManager,
                    repositoryFactory.getGliaEngagementTypeRepository()
            );
        }
        return toggleChatHeadServiceUseCase;
    }

    @NonNull
    public IsDisplayApplicationChatHeadUseCase getIsDisplayApplicationChatHeadUseCase() {
        if (isDisplayApplicationChatHeadUseCase == null) {
            isDisplayApplicationChatHeadUseCase = new IsDisplayApplicationChatHeadUseCase(
                    repositoryFactory.getGliaEngagementRepository(),
                    repositoryFactory.getGliaQueueRepository(),
                    repositoryFactory.getGliaScreenSharingRepository(),
                    permissionManager,
                    gliaSdkConfigurationManager,
                    repositoryFactory.getGliaEngagementTypeRepository()
            );
        }
        return isDisplayApplicationChatHeadUseCase;
    }

    @NonNull
    public ResolveChatHeadNavigationUseCase getResolveChatHeadNavigationUseCase() {
        if (resolveChatHeadNavigationUseCase == null) {
            resolveChatHeadNavigationUseCase = new ResolveChatHeadNavigationUseCase(
                    repositoryFactory.getGliaEngagementRepository(),
                    repositoryFactory.getGliaQueueRepository(),
                    repositoryFactory.getGliaEngagementTypeRepository(),
                    createIsCallVisualizerScreenSharingUseCase()
            );
        }
        return resolveChatHeadNavigationUseCase;
    }

    @NonNull
    public VisitorCodeViewBuilderUseCase getVisitorCodeViewBuilderUseCase() {
        if (visitorCodeViewBuilderUseCase == null) {
            visitorCodeViewBuilderUseCase = new VisitorCodeViewBuilderUseCase();
        }
        return visitorCodeViewBuilderUseCase;
    }

    @NonNull
    public CallNotificationUseCase createCallNotificationUseCase() {
        if (callNotificationUseCase == null)
            callNotificationUseCase = new CallNotificationUseCase(notificationManager);
        return callNotificationUseCase;
    }

    @NonNull
    public ShowScreenSharingNotificationUseCase createShowScreenSharingNotificationUseCase() {
        if (showScreenSharingNotificationUseCase == null)
            showScreenSharingNotificationUseCase = new ShowScreenSharingNotificationUseCase(notificationManager);
        return showScreenSharingNotificationUseCase;
    }

    @NonNull
    public RemoveScreenSharingNotificationUseCase createRemoveScreenSharingNotificationUseCase() {
        if (removeScreenSharingNotificationUseCase == null)
            removeScreenSharingNotificationUseCase = new RemoveScreenSharingNotificationUseCase(notificationManager);
        return removeScreenSharingNotificationUseCase;
    }

    @NonNull
    public GliaLoadHistoryUseCase createGliaLoadHistoryUseCase() {
        return new GliaLoadHistoryUseCase(
                repositoryFactory.getGliaMessageRepository(),
                repositoryFactory.getSecureConversationsRepository(),
                createIsSecureEngagementUseCase(),
                getMapOperatorUseCase(),
                createSubscribeToUnreadMessagesCountUseCase()
        );
    }

    @NonNull
    public MapOperatorUseCase getMapOperatorUseCase() {
        return new MapOperatorUseCase(getOperatorUseCase());
    }

    @NonNull
    public GliaQueueForChatEngagementUseCase createQueueForChatEngagementUseCase() {
        if (gliaQueueForChatEngagementUseCase == null) {
            gliaQueueForChatEngagementUseCase = new GliaQueueForChatEngagementUseCase(
                    schedulers,
                    repositoryFactory.getGliaQueueRepository(),
                    repositoryFactory.getGliaEngagementRepository()
            );
        }
        return gliaQueueForChatEngagementUseCase;
    }

    @NonNull
    public GliaQueueForMediaEngagementUseCase createQueueForMediaEngagementUseCase() {
        if (gliaQueueForMediaEngagementUseCase == null) {
            gliaQueueForMediaEngagementUseCase = new GliaQueueForMediaEngagementUseCase(
                    schedulers,
                    repositoryFactory.getGliaQueueRepository(),
                    repositoryFactory.getGliaEngagementRepository()
            );
        }
        return gliaQueueForMediaEngagementUseCase;
    }

    @NonNull
    public GliaCancelQueueTicketUseCase createCancelQueueTicketUseCase() {
        return new GliaCancelQueueTicketUseCase(
                schedulers,
                repositoryFactory.getGliaQueueRepository()
        );
    }

    @NonNull
    public GliaEndEngagementUseCase createEndEngagementUseCase() {
        return new GliaEndEngagementUseCase(repositoryFactory.getGliaEngagementRepository());
    }

    @NonNull
    public GliaOnEngagementUseCase createOnEngagementUseCase() {
        return new GliaOnEngagementUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                repositoryFactory.getGliaQueueRepository(),
                repositoryFactory.getGliaVisitorMediaRepository(),
                repositoryFactory.getGliaEngagementStateRepository()
        );
    }

    @NonNull
    public GliaOnEngagementEndUseCase createOnEngagementEndUseCase() {
        return new GliaOnEngagementEndUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                repositoryFactory.getGliaFileAttachmentRepository(),
                createOnEngagementUseCase(),
                createCallNotificationUseCase(),
                createRemoveScreenSharingNotificationUseCase(),
                repositoryFactory.getGliaSurveyRepository(),
                repositoryFactory.getGliaVisitorMediaRepository(),
                repositoryFactory.getEngagementConfigRepository()
        );
    }

    @NonNull
    public SetPendingSurveyUseCase createSetPendingSurveyUseCase() {
        return new SetPendingSurveyUseCase(surveyStateManager);
    }

    @NonNull
    public HasPendingSurveyUseCase createHasPendingSurveyUseCase() {
        return new HasPendingSurveyUseCase(surveyStateManager);
    }

    @NonNull
    public SetPendingSurveyUsedUseCase createSetPendingSurveyUsed() {
        return new SetPendingSurveyUsedUseCase(surveyStateManager);
    }

    @NonNull
    public UnengagementMessageUseCase createUnengagementMessageUseCase() {
        return new UnengagementMessageUseCase(
                repositoryFactory.getGliaMessageRepository(),
                repositoryFactory.getGliaEngagementRepository(),
                createOnEngagementUseCase(),
                getMapOperatorUseCase()
        );
    }

    @NonNull
    public GliaOnMessageUseCase createGliaOnMessageUseCase() {
        return new GliaOnMessageUseCase(
                repositoryFactory.getGliaMessageRepository(),
                createOnEngagementUseCase(),
                getMapOperatorUseCase());
    }

    @NonNull
    public GliaOnOperatorTypingUseCase createGliaOnOperatorTypingUseCase() {
        return new GliaOnOperatorTypingUseCase(
                repositoryFactory.getGliaMessageRepository(),
                createOnEngagementUseCase()
        );
    }

    @NonNull
    public GliaSendMessagePreviewUseCase createGliaSendMessagePreviewUseCase() {
        return new GliaSendMessagePreviewUseCase(repositoryFactory.getGliaMessageRepository());
    }

    @NonNull
    public GliaSendMessageUseCase createGliaSendMessageUseCase() {
        return new GliaSendMessageUseCase(
                repositoryFactory.getGliaMessageRepository(),
                repositoryFactory.getGliaFileAttachmentRepository(),
                repositoryFactory.getGliaEngagementStateRepository(),
                repositoryFactory.getEngagementConfigRepository(),
                repositoryFactory.getSecureConversationsRepository(),
                createIsSecureEngagementUseCase()
        );
    }

    @NonNull
    public AddOperatorMediaStateListenerUseCase createAddOperatorMediaStateListenerUseCase() {
        return new AddOperatorMediaStateListenerUseCase(
                repositoryFactory.getGliaOperatorMediaRepository()
        );
    }

    @NonNull
    public ShouldShowMediaEngagementViewUseCase createShouldShowMediaEngagementViewUseCase() {
        return new ShouldShowMediaEngagementViewUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaQueueRepository(),
                repositoryFactory.getGliaEngagementTypeRepository()
        );
    }

    @NonNull
    public AddFileAttachmentsObserverUseCase createAddFileAttachmentsObserverUseCase() {
        return new AddFileAttachmentsObserverUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    @NonNull
    public AddFileToAttachmentAndUploadUseCase createAddFileToAttachmentAndUploadUseCase() {
        return new AddFileToAttachmentAndUploadUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaFileAttachmentRepository(),
                repositoryFactory.getEngagementConfigRepository()
        );
    }

    @NonNull
    public GetFileAttachmentsUseCase createGetFileAttachmentsUseCase() {
        return new GetFileAttachmentsUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    @NonNull
    public RemoveFileAttachmentObserverUseCase createRemoveFileAttachmentObserverUseCase() {
        return new RemoveFileAttachmentObserverUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    @NonNull
    public RemoveFileAttachmentUseCase createRemoveFileAttachmentUseCase() {
        return new RemoveFileAttachmentUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    @NonNull
    public SupportedFileCountCheckUseCase createSupportedFileCountCheckUseCase() {
        return new SupportedFileCountCheckUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    @NonNull
    public IsShowSendButtonUseCase createIsShowSendButtonUseCase() {
        return new IsShowSendButtonUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaFileAttachmentRepository(),
                createIsSecureEngagementUseCase()
        );
    }

    @NonNull
    public HasScreenSharingNotificationChannelEnabledUseCase createHasScreenSharingNotificationChannelEnabledUseCase() {
        return new HasScreenSharingNotificationChannelEnabledUseCase(permissionManager);
    }

    @NonNull
    public IsShowOverlayPermissionRequestDialogUseCase createIsShowOverlayPermissionRequestDialogUseCase() {
        return new IsShowOverlayPermissionRequestDialogUseCase(permissionManager, permissionDialogManager, gliaSdkConfigurationManager);
    }

    @NonNull
    public HasCallNotificationChannelEnabledUseCase createHasCallNotificationChannelEnabledUseCase() {
        return new HasCallNotificationChannelEnabledUseCase(permissionManager);
    }

    @NonNull
    public IsShowEnableCallNotificationChannelDialogUseCase createIsShowEnableCallNotificationChannelDialogUseCase() {
        return new IsShowEnableCallNotificationChannelDialogUseCase(permissionManager, permissionDialogManager);
    }

    @NonNull
    public SetOverlayPermissionRequestDialogShownUseCase createSetOverlayPermissionRequestDialogShownUseCase() {
        return new SetOverlayPermissionRequestDialogShownUseCase(permissionDialogManager);
    }

    @NonNull
    public SetEnableCallNotificationChannelDialogShownUseCase createSetEnableCallNotificationChannelDialogShownUseCase() {
        return new SetEnableCallNotificationChannelDialogShownUseCase(permissionDialogManager);
    }

    @NonNull
    public GetImageFileFromDownloadsUseCase createGetImageFileFromDownloadsUseCase() {
        return new GetImageFileFromDownloadsUseCase(repositoryFactory.getGliaFileRepository());
    }

    @NonNull
    public GetImageFileFromCacheUseCase createGetImageFileFromCacheUseCase() {
        return new GetImageFileFromCacheUseCase(repositoryFactory.getGliaFileRepository());
    }

    @NonNull
    public GetImageFileFromNetworkUseCase createGetImageFileFromNetworkUseCase() {
        return new GetImageFileFromNetworkUseCase(
                repositoryFactory.getGliaFileRepository(),
                createDecodeSampledBitmapFromInputStreamUseCase()
        );
    }

    @NonNull
    public PutImageFileToDownloadsUseCase createPutImageFileToDownloadsUseCase() {
        return new PutImageFileToDownloadsUseCase(repositoryFactory.getGliaFileRepository());
    }

    @NonNull
    public DownloadFileUseCase createDownloadFileUseCase() {
        return new DownloadFileUseCase(repositoryFactory.getGliaFileRepository());
    }

    @NonNull
    public IsEnableChatEditTextUseCase createIsEnableChatEditTextUseCase() {
        return new IsEnableChatEditTextUseCase();
    }

    @NonNull
    public OnNextMessageUseCase createOnNextMessageUseCase() {
        return new OnNextMessageUseCase(repositoryFactory.getSendMessageRepository());
    }

    @NonNull
    public SendMessageButtonStateUseCase createEnableSendMessageButtonUseCase() {
        return new SendMessageButtonStateUseCase(
                repositoryFactory.getSendMessageRepository(),
                repositoryFactory.getSecureFileAttachmentRepository(),
                repositoryFactory.getSecureConversationsRepository(),
                createShowMessageLimitErrorUseCase(),
                schedulers
        );
    }

    @NonNull
    public ShowMessageLimitErrorUseCase createShowMessageLimitErrorUseCase() {
        return new ShowMessageLimitErrorUseCase(
                repositoryFactory.getSendMessageRepository(),
                schedulers
        );
    }

    @NonNull
    public ResetMessageCenterUseCase createResetMessageCenterUseCase() {
        return new ResetMessageCenterUseCase(
                repositoryFactory.getSecureFileAttachmentRepository(),
                repositoryFactory.getSendMessageRepository()
        );
    }

    @NonNull
    public SiteInfoUseCase createSiteInfoUseCase() {
        return new SiteInfoUseCase(repositoryFactory.getGliaEngagementRepository());
    }

    @NonNull
    public GliaSurveyUseCase getGliaSurveyUseCase() {
        return new GliaSurveyUseCase(repositoryFactory.getGliaSurveyRepository());
    }

    @NonNull
    public GliaSurveyAnswerUseCase getSurveyAnswerUseCase() {
        return new GliaSurveyAnswerUseCase(repositoryFactory.getGliaSurveyRepository());
    }

    @NonNull
    public AddVisitorMediaStateListenerUseCase createAddVisitorMediaStateListenerUseCase() {
        return new AddVisitorMediaStateListenerUseCase(repositoryFactory.getGliaVisitorMediaRepository());
    }

    @NonNull
    public RemoveVisitorMediaStateListenerUseCase createRemoveVisitorMediaStateListenerUseCase() {
        return new RemoveVisitorMediaStateListenerUseCase(repositoryFactory.getGliaVisitorMediaRepository());
    }

    @NonNull
    public ToggleVisitorAudioMediaMuteUseCase createToggleVisitorAudioMediaMuteUseCase() {
        return new ToggleVisitorAudioMediaMuteUseCase(
                schedulers,
                repositoryFactory.getGliaVisitorMediaRepository()
        );
    }

    @NonNull
    public ToggleVisitorVideoUseCase createToggleVisitorVideoUseCase() {
        return new ToggleVisitorVideoUseCase(
                schedulers,
                repositoryFactory.getGliaVisitorMediaRepository()
        );
    }

    @NonNull
    public IsShowVideoUseCase createIsShowVideoUseCase() {
        return new IsShowVideoUseCase(schedulers);
    }

    @NonNull
    public IsShowOnHoldUseCase createIsShowOnHoldUseCase() {
        return new IsShowOnHoldUseCase(schedulers);
    }

    @NonNull
    public GetEngagementStateFlowableUseCase createGetGliaEngagementStateFlowableUseCase() {
        return new GetEngagementStateFlowableUseCase(repositoryFactory.getGliaEngagementStateRepository());
    }

    @NonNull
    public GetOperatorFlowableUseCase createGetOperatorFlowableUseCase() {
        return new GetOperatorFlowableUseCase(repositoryFactory.getGliaEngagementStateRepository());
    }

    @NonNull
    public IsFromCallScreenUseCase createIsFromCallScreenUseCase() {
        return new IsFromCallScreenUseCase(repositoryFactory.getChatScreenRepository());
    }

    @NonNull
    public UpdateFromCallScreenUseCase createUpdateFromCallScreenUseCase() {
        return new UpdateFromCallScreenUseCase(repositoryFactory.getChatScreenRepository());
    }

    @NonNull
    public GetOperatorUseCase getOperatorUseCase() {
        return new GetOperatorUseCase(repositoryFactory.getOperatorRepository());
    }

    @NonNull
    public CustomCardTypeUseCase createCustomCardTypeUseCase() {
        return new CustomCardTypeUseCase(GliaWidgets.getCustomCardAdapter());
    }

    @NonNull
    public CustomCardAdapterTypeUseCase createCustomCardAdapterTypeUseCase() {
        return new CustomCardAdapterTypeUseCase(GliaWidgets.getCustomCardAdapter());
    }

    @NonNull
    public CustomCardInteractableUseCase createCustomCardInteractableUseCase() {
        return new CustomCardInteractableUseCase(GliaWidgets.getCustomCardAdapter());
    }

    @NonNull
    public CustomCardShouldShowUseCase createCustomCardShouldShowUseCase() {
        return new CustomCardShouldShowUseCase(GliaWidgets.getCustomCardAdapter());
    }

    @NonNull
    public QueueTicketStateChangeToUnstaffedUseCase createQueueTicketStateChangeToUnstaffedUseCase() {
        return new QueueTicketStateChangeToUnstaffedUseCase(repositoryFactory.getGliaQueueRepository());
    }

    @NonNull
    public IsCallOrChatScreenActiveUseCase createIsCallOrChatScreenActiveUseCase() {
        return new IsCallOrChatScreenActiveUseCase();
    }

    @NonNull
    public IsCallVisualizerUseCase createIsCallVisualizerUseCase() {
        return new IsCallVisualizerUseCase(repositoryFactory.getGliaEngagementRepository());
    }

    @NonNull
    public IsCallVisualizerScreenSharingUseCase createIsCallVisualizerScreenSharingUseCase() {
        return new IsCallVisualizerScreenSharingUseCase(repositoryFactory.getGliaEngagementTypeRepository());
    }

    @NonNull
    public AddMediaUpgradeOfferCallbackUseCase createAddMediaUpgradeOfferCallbackUseCase() {
        return new AddMediaUpgradeOfferCallbackUseCase(repositoryFactory.getMediaUpgradeOfferRepository());
    }

    @NonNull
    public RemoveMediaUpgradeOfferCallbackUseCase createRemoveMediaUpgradeOfferCallbackUseCase() {
        return new RemoveMediaUpgradeOfferCallbackUseCase(repositoryFactory.getMediaUpgradeOfferRepository());
    }

    @NonNull
    public SendSecureMessageUseCase createSendSecureMessageUseCase(String queueId) {
        return new SendSecureMessageUseCase(
                queueId,
                repositoryFactory.getSendMessageRepository(),
                repositoryFactory.getSecureConversationsRepository(),
                repositoryFactory.getSecureFileAttachmentRepository());
    }

    @NonNull
    public IsMessageCenterAvailableUseCase createIsMessageCenterAvailableUseCase(String queueId) {
        return new IsMessageCenterAvailableUseCase(queueId, createIsMessagingAvailableUseCase());
    }

    @NonNull
    public AddSecureFileToAttachmentAndUploadUseCase createAddSecureFileToAttachmentAndUploadUseCase() {
        return new AddSecureFileToAttachmentAndUploadUseCase(repositoryFactory.getSecureFileAttachmentRepository());
    }

    @NonNull
    public AddSecureFileAttachmentsObserverUseCase createAddSecureFileAttachmentsObserverUseCase() {
        return new AddSecureFileAttachmentsObserverUseCase(
                repositoryFactory.getSecureFileAttachmentRepository(),
                schedulers
        );
    }

    @NonNull
    public GetSecureFileAttachmentsUseCase createGetSecureFileAttachmentsUseCase() {
        return new GetSecureFileAttachmentsUseCase(repositoryFactory.getSecureFileAttachmentRepository());
    }

    @NonNull
    public RemoveSecureFileAttachmentUseCase createRemoveSecureFileAttachmentUseCase() {
        return new RemoveSecureFileAttachmentUseCase(repositoryFactory.getSecureFileAttachmentRepository());
    }

    @NonNull
    public IsSecureEngagementUseCase createIsSecureEngagementUseCase() {
        return new IsSecureEngagementUseCase(
                repositoryFactory.getEngagementConfigRepository(),
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaQueueRepository()
        );
    }

    @NonNull
    public IsAuthenticatedUseCase createIsAuthenticatedUseCase() {
        return new IsAuthenticatedUseCase(gliaCore.getAuthentication(Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT));
    }

    @NonNull
    public SetEngagementConfigUseCase createSetEngagementConfigUseCase() {
        return new SetEngagementConfigUseCase(repositoryFactory.getEngagementConfigRepository());
    }

    @NonNull
    public IsQueueingEngagementUseCase createIsQueueingEngagementUseCase() {
        return new IsQueueingEngagementUseCase(repositoryFactory.getGliaQueueRepository());
    }

    @NonNull
    public IsOngoingEngagementUseCase createIsOngoingEngagementUseCase() {
        return new IsOngoingEngagementUseCase(repositoryFactory.getGliaEngagementRepository());
    }

    @NonNull
    public IsMessagingAvailableUseCase createIsMessagingAvailableUseCase() {
        return new IsMessagingAvailableUseCase(repositoryFactory.getGliaQueueRepository(), schedulers);
    }

    @NonNull
    public IsSecureConversationsChatAvailableUseCase createIsSecureConversationsChatAvailableUseCase() {
        return new IsSecureConversationsChatAvailableUseCase(
                repositoryFactory.getEngagementConfigRepository(),
                createIsMessagingAvailableUseCase()
        );
    }

    @NonNull
    public GetUnreadMessagesCountWithTimeoutUseCase createSubscribeToUnreadMessagesCountUseCase() {
        return new GetUnreadMessagesCountWithTimeoutUseCase(repositoryFactory.getSecureConversationsRepository());
    }

    @NonNull
    public MarkMessagesReadWithDelayUseCase createMarkMessagesReadUseCase() {
        return new MarkMessagesReadWithDelayUseCase(repositoryFactory.getSecureConversationsRepository());
    }

    @NonNull
    public GliaOnCallVisualizerUseCase createOnCallVisualizerUseCase() {
        return new GliaOnCallVisualizerUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                repositoryFactory.getGliaQueueRepository(),
                repositoryFactory.getGliaVisitorMediaRepository(),
                repositoryFactory.getGliaEngagementStateRepository()
        );
    }

    @NonNull
    public GliaOnCallVisualizerEndUseCase createOnCallVisualizerEndUseCase() {
        return new GliaOnCallVisualizerEndUseCase(
                repositoryFactory.getCallVisualizerRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                createOnCallVisualizerUseCase(),
                callNotificationUseCase,
                removeScreenSharingNotificationUseCase,
                repositoryFactory.getGliaSurveyRepository(),
                repositoryFactory.getGliaVisitorMediaRepository()
        );
    }

    @NonNull
    public FindNewMessagesDividerIndexUseCase createFindNewMessagesDividerIndexUseCase() {
        return new FindNewMessagesDividerIndexUseCase();
    }

    @NonNull
    public AddNewMessagesDividerUseCase createAddNewMessagesDividerUseCase() {
        return new AddNewMessagesDividerUseCase(createFindNewMessagesDividerIndexUseCase());
    }

    @NonNull
    DecodeSampledBitmapFromInputStreamUseCase createDecodeSampledBitmapFromInputStreamUseCase() {
        return new DecodeSampledBitmapFromInputStreamUseCase();
    }
}
