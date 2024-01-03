package com.glia.widgets.di;

import androidx.annotation.NonNull;

import com.glia.androidsdk.visitor.Authentication;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.StringProvider;
import com.glia.widgets.call.domain.HandleCallPermissionsUseCase;
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase;
import com.glia.widgets.chat.domain.AddNewMessagesDividerUseCase;
import com.glia.widgets.chat.domain.AppendGvaMessageItemUseCase;
import com.glia.widgets.chat.domain.AppendHistoryChatMessageUseCase;
import com.glia.widgets.chat.domain.AppendHistoryCustomCardItemUseCase;
import com.glia.widgets.chat.domain.AppendHistoryOperatorChatItemUseCase;
import com.glia.widgets.chat.domain.AppendHistoryResponseCardOrTextItemUseCase;
import com.glia.widgets.chat.domain.AppendHistoryVisitorChatItemUseCase;
import com.glia.widgets.chat.domain.AppendNewChatMessageUseCase;
import com.glia.widgets.chat.domain.AppendNewOperatorMessageUseCase;
import com.glia.widgets.chat.domain.AppendNewResponseCardOrTextItemUseCase;
import com.glia.widgets.chat.domain.AppendNewVisitorMessageUseCase;
import com.glia.widgets.chat.domain.AppendSystemMessageItemUseCase;
import com.glia.widgets.chat.domain.CustomCardAdapterTypeUseCase;
import com.glia.widgets.chat.domain.CustomCardShouldShowUseCase;
import com.glia.widgets.chat.domain.CustomCardTypeUseCase;
import com.glia.widgets.chat.domain.DecodeSampledBitmapFromInputStreamUseCase;
import com.glia.widgets.chat.domain.FindNewMessagesDividerIndexUseCase;
import com.glia.widgets.chat.domain.GliaLoadHistoryUseCase;
import com.glia.widgets.chat.domain.GliaOnMessageUseCase;
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase;
import com.glia.widgets.chat.domain.GliaSendMessageUseCase;
import com.glia.widgets.chat.domain.HandleCustomCardClickUseCase;
import com.glia.widgets.chat.domain.IsAuthenticatedUseCase;
import com.glia.widgets.chat.domain.IsFromCallScreenUseCase;
import com.glia.widgets.chat.domain.IsSecureConversationsChatAvailableUseCase;
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase;
import com.glia.widgets.chat.domain.MapOperatorAttachmentUseCase;
import com.glia.widgets.chat.domain.MapOperatorPlainTextUseCase;
import com.glia.widgets.chat.domain.MapResponseCardUseCase;
import com.glia.widgets.chat.domain.MapVisitorAttachmentUseCase;
import com.glia.widgets.chat.domain.SendUnsentMessagesUseCase;
import com.glia.widgets.chat.domain.SiteInfoUseCase;
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase;
import com.glia.widgets.chat.domain.gva.DetermineGvaButtonTypeUseCase;
import com.glia.widgets.chat.domain.gva.DetermineGvaUrlTypeUseCase;
import com.glia.widgets.chat.domain.gva.GetGvaTypeUseCase;
import com.glia.widgets.chat.domain.gva.IsGvaUseCase;
import com.glia.widgets.chat.domain.gva.MapGvaGvaGalleryCardsUseCase;
import com.glia.widgets.chat.domain.gva.MapGvaPersistentButtonsUseCase;
import com.glia.widgets.chat.domain.gva.MapGvaQuickRepliesUseCase;
import com.glia.widgets.chat.domain.gva.MapGvaResponseTextUseCase;
import com.glia.widgets.chat.domain.gva.MapGvaUseCase;
import com.glia.widgets.chat.domain.gva.ParseGvaButtonsUseCase;
import com.glia.widgets.chat.domain.gva.ParseGvaGalleryCardsUseCase;
import com.glia.widgets.core.audio.AudioControlManager;
import com.glia.widgets.core.audio.domain.OnAudioStartedUseCase;
import com.glia.widgets.core.audio.domain.TurnSpeakerphoneUseCase;
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase;
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase;
import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.chathead.domain.IsDisplayApplicationChatHeadUseCase;
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase;
import com.glia.widgets.core.chathead.domain.ToggleChatHeadServiceUseCase;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase;
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCase;
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase;
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase;
import com.glia.widgets.core.engagement.domain.GetOperatorUseCase;
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase;
import com.glia.widgets.core.engagement.domain.SetEngagementConfigUseCase;
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase;
import com.glia.widgets.core.engagement.domain.UpdateOperatorDefaultImageUrlUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase;
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.domain.CallNotificationUseCase;
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.core.permissions.domain.HasCallNotificationChannelEnabledUseCase;
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase;
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
import com.glia.widgets.engagement.AcceptMediaUpgradeOfferUseCase;
import com.glia.widgets.engagement.CurrentOperatorUseCase;
import com.glia.widgets.engagement.DeclineMediaUpgradeOfferUseCase;
import com.glia.widgets.engagement.EndEngagementUseCase;
import com.glia.widgets.engagement.EngagementRequestUseCase;
import com.glia.widgets.engagement.EngagementStateUseCase;
import com.glia.widgets.engagement.EngagementTypeUseCase;
import com.glia.widgets.engagement.EnqueueForEngagementUseCase;
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizerUseCase;
import com.glia.widgets.engagement.IsOperatorPresentUseCase;
import com.glia.widgets.engagement.IsQueueingOrEngagementUseCase;
import com.glia.widgets.engagement.MediaUpgradeOfferUseCase;
import com.glia.widgets.engagement.OperatorMediaUseCase;
import com.glia.widgets.engagement.OperatorTypingUseCase;
import com.glia.widgets.engagement.ReleaseResourcesUseCase;
import com.glia.widgets.engagement.ScreenSharingUseCase;
import com.glia.widgets.engagement.SurveyUseCase;
import com.glia.widgets.engagement.ToggleVisitorAudioMediaStateUseCase;
import com.glia.widgets.engagement.ToggleVisitorVideoMediaStateUseCase;
import com.glia.widgets.engagement.VisitorMediaUseCase;
import com.glia.widgets.engagement.completion.EngagementCompletionUseCase;
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;
import com.glia.widgets.filepreview.domain.usecase.IsFileReadyForPreviewUseCase;
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase;
import com.glia.widgets.helper.rx.Schedulers;
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowOnHoldUseCase;
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowVideoUseCase;
import com.glia.widgets.view.snackbar.LiveObservationPopupUseCase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UseCaseFactory {
    private static CallNotificationUseCase callNotificationUseCase;
    private static ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private static RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private static ToggleChatHeadServiceUseCase toggleChatHeadServiceUseCase;
    private static IsDisplayApplicationChatHeadUseCase isDisplayApplicationChatHeadUseCase;
    private static ResolveChatHeadNavigationUseCase resolveChatHeadNavigationUseCase;
    private static VisitorCodeViewBuilderUseCase visitorCodeViewBuilderUseCase;
    private final RepositoryFactory repositoryFactory;
    private final PermissionManager permissionManager;
    private final PermissionDialogManager permissionDialogManager;
    private final GliaSdkConfigurationManager gliaSdkConfigurationManager;
    private final INotificationManager notificationManager;
    private final ChatHeadManager chatHeadManager;
    private final AudioControlManager audioControlManager;
    private final Schedulers schedulers;
    private final StringProvider stringProvider;
    private final GliaCore gliaCore;

    private Gson gvaGson;

    public UseCaseFactory(RepositoryFactory repositoryFactory,
                          PermissionManager permissionManager,
                          PermissionDialogManager permissionDialogManager,
                          INotificationManager notificationManager,
                          GliaSdkConfigurationManager gliaSdkConfigurationManager,
                          ChatHeadManager chatHeadManager,
                          AudioControlManager audioControlManager,
                          Schedulers schedulers,
                          StringProvider stringProvider,
                          GliaCore gliaCore) {
        this.repositoryFactory = repositoryFactory;
        this.permissionManager = permissionManager;
        this.permissionDialogManager = permissionDialogManager;
        this.notificationManager = notificationManager;
        this.gliaSdkConfigurationManager = gliaSdkConfigurationManager;
        this.chatHeadManager = chatHeadManager;
        this.audioControlManager = audioControlManager;
        this.schedulers = schedulers;
        this.stringProvider = stringProvider;
        this.gliaCore = gliaCore;
    }

    @NonNull
    private AppendHistoryResponseCardOrTextItemUseCase createAppendHistoryResponseCardOrTextItemUseCase() {
        return new AppendHistoryResponseCardOrTextItemUseCase(
            createMapOperatorAttachmentUseCase(),
            createMapOperatorPlainTextUseCase(),
            createMapResponseCardUseCase()
        );
    }

    @NonNull
    public MapResponseCardUseCase createMapResponseCardUseCase() {
        return new MapResponseCardUseCase();
    }

    @NonNull
    public MapOperatorAttachmentUseCase createMapOperatorAttachmentUseCase() {
        return new MapOperatorAttachmentUseCase();
    }

    @NonNull
    public MapOperatorPlainTextUseCase createMapOperatorPlainTextUseCase() {
        return new MapOperatorPlainTextUseCase();
    }

    @NonNull
    public ToggleChatHeadServiceUseCase getToggleChatHeadServiceUseCase() {
        if (toggleChatHeadServiceUseCase == null) {
            toggleChatHeadServiceUseCase = new ToggleChatHeadServiceUseCase(
                getIsQueueingOrEngagementUseCase(),
                getIsCurrentEngagementCallVisualizer(),
                getScreenSharingUseCase(),
                chatHeadManager,
                permissionManager,
                gliaSdkConfigurationManager,
                getEngagementTypeUseCase()
            );
        }
        return toggleChatHeadServiceUseCase;
    }

    @NonNull
    public IsDisplayApplicationChatHeadUseCase getIsDisplayApplicationChatHeadUseCase() {
        if (isDisplayApplicationChatHeadUseCase == null) {
            isDisplayApplicationChatHeadUseCase = new IsDisplayApplicationChatHeadUseCase(
                getIsQueueingOrEngagementUseCase(),
                getIsCurrentEngagementCallVisualizer(),
                getScreenSharingUseCase(),
                permissionManager,
                gliaSdkConfigurationManager,
                getEngagementTypeUseCase()
            );
        }
        return isDisplayApplicationChatHeadUseCase;
    }

    @NonNull
    public ResolveChatHeadNavigationUseCase getResolveChatHeadNavigationUseCase() {
        if (resolveChatHeadNavigationUseCase == null) {
            resolveChatHeadNavigationUseCase = new ResolveChatHeadNavigationUseCase(
                getIsQueueingOrEngagementUseCase(),
                getEngagementTypeUseCase(),
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
        return new MapOperatorUseCase(createGetOperatorUseCase());
    }

    @NonNull
    public GliaOnMessageUseCase createGliaOnMessageUseCase() {
        return new GliaOnMessageUseCase(
            repositoryFactory.getGliaMessageRepository(),
            getMapOperatorUseCase()
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
            getIsOperatorPresentUseCase(),
            repositoryFactory.getEngagementConfigRepository(),
            repositoryFactory.getSecureConversationsRepository(),
            createIsSecureEngagementUseCase()
        );
    }

    @NonNull
    public ShouldShowMediaEngagementViewUseCase createShouldShowMediaEngagementViewUseCase() {
        return new ShouldShowMediaEngagementViewUseCase(
            getIsQueueingOrEngagementUseCase(),
            getEngagementTypeUseCase()
        );
    }

    @NonNull
    public AddFileAttachmentsObserverUseCase createAddFileAttachmentsObserverUseCase() {
        return new AddFileAttachmentsObserverUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    @NonNull
    public AddFileToAttachmentAndUploadUseCase createAddFileToAttachmentAndUploadUseCase() {
        return new AddFileToAttachmentAndUploadUseCase(
            getIsQueueingOrEngagementUseCase(),
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
            getIsQueueingOrEngagementUseCase(),
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
        return new SiteInfoUseCase(gliaCore);
    }

    @NonNull
    public GliaSurveyAnswerUseCase getSurveyAnswerUseCase() {
        return new GliaSurveyAnswerUseCase(repositoryFactory.getGliaSurveyRepository());
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
    public IsFromCallScreenUseCase createIsFromCallScreenUseCase() {
        return new IsFromCallScreenUseCase(repositoryFactory.getChatScreenRepository());
    }

    @NonNull
    public UpdateFromCallScreenUseCase createUpdateFromCallScreenUseCase() {
        return new UpdateFromCallScreenUseCase(repositoryFactory.getChatScreenRepository());
    }

    @NonNull
    public GetOperatorUseCase createGetOperatorUseCase() {
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
    public CustomCardShouldShowUseCase createCustomCardShouldShowUseCase() {
        return new CustomCardShouldShowUseCase(GliaWidgets.getCustomCardAdapter());
    }

    @NonNull
    public IsCallOrChatScreenActiveUseCase createIsCallOrChatScreenActiveUseCase() {
        return new IsCallOrChatScreenActiveUseCase();
    }

    @NonNull
    public IsCallVisualizerScreenSharingUseCase createIsCallVisualizerScreenSharingUseCase() {
        return new IsCallVisualizerScreenSharingUseCase(getEngagementTypeUseCase());
    }

    @NonNull
    public SendSecureMessageUseCase createSendSecureMessageUseCase(String queueId) {
        return new SendSecureMessageUseCase(
            queueId,
            repositoryFactory.getSendMessageRepository(),
            repositoryFactory.getSecureConversationsRepository(),
            repositoryFactory.getSecureFileAttachmentRepository(),
            repositoryFactory.getGliaMessageRepository(),
            getIsQueueingOrEngagementUseCase()
        );
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
            getIsQueueingOrEngagementUseCase()
        );
    }

    @NonNull
    public IsAuthenticatedUseCase createIsAuthenticatedUseCase() {
        return new IsAuthenticatedUseCase(gliaCore.getAuthentication(Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT));
    }

    @NonNull
    public SetEngagementConfigUseCase createSetEngagementConfigUseCase() {
        return new SetEngagementConfigUseCase(
            repositoryFactory.getEngagementConfigRepository(),
            repositoryFactory.getEngagementRepository()
        );
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
    public FindNewMessagesDividerIndexUseCase createFindNewMessagesDividerIndexUseCase() {
        return new FindNewMessagesDividerIndexUseCase();
    }

    @NonNull
    public AddNewMessagesDividerUseCase createAddNewMessagesDividerUseCase() {
        return new AddNewMessagesDividerUseCase(createFindNewMessagesDividerIndexUseCase());
    }

    @NonNull
    public IsFileReadyForPreviewUseCase createIsFileReadyForPreviewUseCase() {
        return new IsFileReadyForPreviewUseCase(repositoryFactory.getGliaFileRepository());
    }

    @NonNull
    DecodeSampledBitmapFromInputStreamUseCase createDecodeSampledBitmapFromInputStreamUseCase() {
        return new DecodeSampledBitmapFromInputStreamUseCase();
    }

    @NonNull
    public OnAudioStartedUseCase createOnAudioStartedUseCase() {
        return new OnAudioStartedUseCase(getOperatorMediaUseCase(), getVisitorMediaUseCase());
    }

    @NonNull
    public TurnSpeakerphoneUseCase createTurnSpeakerphoneUseCase() {
        return new TurnSpeakerphoneUseCase(
            audioControlManager
        );
    }

    @NonNull
    public HandleCallPermissionsUseCase createHandleCallPermissionsUseCase() {
        return new HandleCallPermissionsUseCase(
            getIsCurrentEngagementCallVisualizer(),
            permissionManager
        );
    }

    @NonNull
    private Gson getGvaGson() {
        if (gvaGson == null) {
            gvaGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        }

        return gvaGson;
    }

    @NonNull
    public ParseGvaButtonsUseCase createParseGvaButtonsUseCase() {
        return new ParseGvaButtonsUseCase(getGvaGson());
    }

    @NonNull
    public ParseGvaGalleryCardsUseCase createParseGvaGalleryCardsUseCase() {
        return new ParseGvaGalleryCardsUseCase(getGvaGson());
    }

    @NonNull
    public GetGvaTypeUseCase createGetGvaTypeUseCase() {
        return new GetGvaTypeUseCase();
    }

    @NonNull
    public IsGvaUseCase createIsGvaUseCase() {
        return new IsGvaUseCase(createGetGvaTypeUseCase());
    }

    @NonNull
    public MapGvaResponseTextUseCase createMapGvaResponseTextUseCase() {
        return new MapGvaResponseTextUseCase();
    }

    @NonNull
    public MapGvaPersistentButtonsUseCase createMapGvaPersistentButtonsUseCase() {
        return new MapGvaPersistentButtonsUseCase(createParseGvaButtonsUseCase());
    }

    @NonNull
    public MapGvaQuickRepliesUseCase createMapGvaGvaQuickRepliesUseCase() {
        return new MapGvaQuickRepliesUseCase(createParseGvaButtonsUseCase());
    }

    @NonNull
    public MapGvaGvaGalleryCardsUseCase createMapGvaGvaGalleryCardsUseCase() {
        return new MapGvaGvaGalleryCardsUseCase(createParseGvaGalleryCardsUseCase());
    }

    @NonNull
    public MapGvaUseCase createMapGvaUseCase() {
        return new MapGvaUseCase(
            createGetGvaTypeUseCase(),
            createMapGvaResponseTextUseCase(),
            createMapGvaPersistentButtonsUseCase(),
            createMapGvaGvaQuickRepliesUseCase(),
            createMapGvaGvaGalleryCardsUseCase()
        );
    }

    @NonNull
    public DetermineGvaUrlTypeUseCase createDetermineGvaUrlTypeUseCase() {
        return new DetermineGvaUrlTypeUseCase();
    }

    @NonNull
    public DetermineGvaButtonTypeUseCase createDetermineGvaButtonTypeUseCase() {
        return new DetermineGvaButtonTypeUseCase(createDetermineGvaUrlTypeUseCase());
    }

    @NonNull
    public HandleCustomCardClickUseCase createHandleCustomCardClickUseCase() {
        return new HandleCustomCardClickUseCase(
            createCustomCardTypeUseCase(),
            createCustomCardShouldShowUseCase()
        );
    }

    @NonNull
    public AppendHistoryChatMessageUseCase createAppendHistoryChatMessageUseCase() {
        return new AppendHistoryChatMessageUseCase(
            createAppendHistoryVisitorChatItemUseCase(),
            createAppendHistoryOperatorChatItemUseCase(),
            createAppendSystemMessageItemUseCase()
        );
    }

    @NonNull
    public AppendSystemMessageItemUseCase createAppendSystemMessageItemUseCase() {
        return new AppendSystemMessageItemUseCase();
    }

    @NonNull
    public AppendHistoryVisitorChatItemUseCase createAppendHistoryVisitorChatItemUseCase() {
        return new AppendHistoryVisitorChatItemUseCase(createMapVisitorAttachmentUseCase());
    }

    @NonNull
    public MapVisitorAttachmentUseCase createMapVisitorAttachmentUseCase() {
        return new MapVisitorAttachmentUseCase();
    }

    @NonNull
    public AppendHistoryOperatorChatItemUseCase createAppendHistoryOperatorChatItemUseCase() {
        return new AppendHistoryOperatorChatItemUseCase(
            createIsGvaUseCase(),
            createCustomCardAdapterTypeUseCase(),
            createAppendHistoryGvaMessageItemUseCase(),
            createAppendHistoryCustomCardItemUseCase(),
            createAppendHistoryResponseCardOrTextItemUseCase()
        );
    }

    @NonNull
    public AppendHistoryCustomCardItemUseCase createAppendHistoryCustomCardItemUseCase() {
        return new AppendHistoryCustomCardItemUseCase(
            createCustomCardTypeUseCase(),
            createCustomCardShouldShowUseCase()
        );
    }

    @NonNull
    public AppendGvaMessageItemUseCase createAppendHistoryGvaMessageItemUseCase() {
        return new AppendGvaMessageItemUseCase(createMapGvaUseCase());
    }

    @NonNull
    public AppendNewVisitorMessageUseCase createAppendNewVisitorMessageUseCase() {
        return new AppendNewVisitorMessageUseCase(createMapVisitorAttachmentUseCase());
    }

    @NonNull
    public AppendNewOperatorMessageUseCase createAppendNewOperatorMessageUseCase() {
        return new AppendNewOperatorMessageUseCase(
            createIsGvaUseCase(),
            createCustomCardAdapterTypeUseCase(),
            createAppendGvaMessageItemUseCase(),
            createAppendHistoryCustomCardItemUseCase(),
            createAppendNewResponseCardOrTextItemUseCase()
        );
    }

    @NonNull
    private AppendNewResponseCardOrTextItemUseCase createAppendNewResponseCardOrTextItemUseCase() {
        return new AppendNewResponseCardOrTextItemUseCase(
            createMapOperatorAttachmentUseCase(),
            createMapOperatorPlainTextUseCase(),
            createMapResponseCardUseCase()
        );
    }

    @NonNull
    public AppendGvaMessageItemUseCase createAppendGvaMessageItemUseCase() {
        return new AppendGvaMessageItemUseCase(createMapGvaUseCase());
    }

    @NonNull
    public AppendNewChatMessageUseCase createAppendNewChatMessageUseCase() {
        return new AppendNewChatMessageUseCase(
            createAppendNewOperatorMessageUseCase(),
            createAppendNewVisitorMessageUseCase(),
            createAppendSystemMessageItemUseCase()
        );
    }

    @NonNull
    public SendUnsentMessagesUseCase createSendUnsentMessagesUseCase() {
        return new SendUnsentMessagesUseCase(
            repositoryFactory.getGliaMessageRepository(),
            repositoryFactory.getSecureConversationsRepository(),
            repositoryFactory.getEngagementConfigRepository(),
            createIsSecureEngagementUseCase()
        );
    }

    @NonNull
    public UpdateOperatorDefaultImageUrlUseCase createUpdateOperatorDefaultImageUrlUseCase() {
        return new UpdateOperatorDefaultImageUrlUseCase(repositoryFactory.getOperatorRepository(), createSiteInfoUseCase());
    }

    @NonNull
    public ConfirmationDialogUseCase createConfirmationDialogUseCase() {
        return new ConfirmationDialogUseCase(createSiteInfoUseCase());
    }

    @NonNull
    public ConfirmationDialogLinksUseCase createConfirmationDialogLinksUseCase() {
        return new ConfirmationDialogLinksUseCase(stringProvider);
    }

    public LiveObservationPopupUseCase createLiveObservationPopupUseCase() {
        return new LiveObservationPopupUseCase(createSiteInfoUseCase());
    }

    @NonNull
    public EndEngagementUseCase getEndEngagementUseCase() {
        return new EndEngagementUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public IsQueueingOrEngagementUseCase getIsQueueingOrEngagementUseCase() {
        return new IsQueueingOrEngagementUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public IsCurrentEngagementCallVisualizerUseCase getIsCurrentEngagementCallVisualizer() {
        return new IsCurrentEngagementCallVisualizerUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public SurveyUseCase getSurveyUseCase() {
        return new SurveyUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EngagementStateUseCase getEngagementStateUseCase() {
        return new EngagementStateUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public ReleaseResourcesUseCase getReleaseResourcesUseCase(DialogController dialogController) {
        return new ReleaseResourcesUseCase(
            createRemoveScreenSharingNotificationUseCase(),
            createCallNotificationUseCase(),
            repositoryFactory.getGliaFileAttachmentRepository(),
            repositoryFactory.getEngagementConfigRepository(),
            createUpdateFromCallScreenUseCase(),
            dialogController
        );
    }

    @NonNull
    public OperatorTypingUseCase operatorTypingUseCase() {
        return new OperatorTypingUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public OperatorMediaUseCase getOperatorMediaUseCase() {
        return new OperatorMediaUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public VisitorMediaUseCase getVisitorMediaUseCase() {
        return new VisitorMediaUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EngagementTypeUseCase getEngagementTypeUseCase() {
        return new EngagementTypeUseCase(
            getIsQueueingOrEngagementUseCase(),
            getIsCurrentEngagementCallVisualizer(),
            getOperatorMediaUseCase(),
            getVisitorMediaUseCase(),
            getIsOperatorPresentUseCase()
        );
    }

    @NonNull
    public CurrentOperatorUseCase getCurrentOperatorUseCase() {
        return new CurrentOperatorUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public IsOperatorPresentUseCase getIsOperatorPresentUseCase() {
        return new IsOperatorPresentUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public MediaUpgradeOfferUseCase getMediaUpgradeOfferUseCase() {
        return new MediaUpgradeOfferUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public AcceptMediaUpgradeOfferUseCase getAcceptMediaUpgradeOfferUseCase() {
        return new AcceptMediaUpgradeOfferUseCase(repositoryFactory.getEngagementRepository(), permissionManager);
    }

    @NonNull
    public DeclineMediaUpgradeOfferUseCase getDeclineMediaUpgradeOfferUseCase() {
        return new DeclineMediaUpgradeOfferUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EngagementRequestUseCase getEngagementRequestUseCase() {
        return new EngagementRequestUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public ToggleVisitorAudioMediaStateUseCase getToggleVisitorAudioMediaStateUseCase() {
        return new ToggleVisitorAudioMediaStateUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public ToggleVisitorVideoMediaStateUseCase getToggleVisitorVideoMediaStateUseCase() {
        return new ToggleVisitorVideoMediaStateUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EnqueueForEngagementUseCase getQueueForEngagementUseCase() {
        return new EnqueueForEngagementUseCase(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EngagementCompletionUseCase getEngagementCompletionUseCase() {
        return new EngagementCompletionUseCase(getEngagementStateUseCase(), getSurveyUseCase());
    }

    @NonNull
    public ScreenSharingUseCase getScreenSharingUseCase() {
        return new ScreenSharingUseCase(repositoryFactory.getEngagementRepository());
    }

}
