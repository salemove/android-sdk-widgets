package com.glia.widgets.di;

import android.content.Context;

import androidx.annotation.NonNull;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.StringProvider;
import com.glia.widgets.call.domain.HandleCallPermissionsUseCase;
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
import com.glia.widgets.chat.domain.DecideOnQueueingUseCase;
import com.glia.widgets.chat.domain.DecideOnQueueingUseCaseImpl;
import com.glia.widgets.chat.domain.DecodeSampledBitmapFromInputStreamUseCase;
import com.glia.widgets.chat.domain.FileProviderUseCase;
import com.glia.widgets.chat.domain.FileProviderUseCaseImpl;
import com.glia.widgets.chat.domain.FindNewMessagesDividerIndexUseCase;
import com.glia.widgets.chat.domain.FixCapturedPictureRotationUseCase;
import com.glia.widgets.chat.domain.FixCapturedPictureRotationUseCaseImpl;
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
import com.glia.widgets.chat.domain.TakePictureUseCase;
import com.glia.widgets.chat.domain.TakePictureUseCaseImpl;
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase;
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCase;
import com.glia.widgets.chat.domain.UriToFileAttachmentUseCaseImpl;
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
import com.glia.widgets.core.dialog.DialogContract;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase;
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase;
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCaseImpl;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCaseImpl;
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCase;
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCaseImpl;
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase;
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCaseImpl;
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
import com.glia.widgets.core.permissions.domain.IsNotificationPermissionGrantedUseCase;
import com.glia.widgets.core.permissions.domain.IsNotificationPermissionGrantedUseCaseImpl;
import com.glia.widgets.core.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCase;
import com.glia.widgets.core.permissions.domain.RequestNotificationPermissionIfPushNotificationsSetUpUseCaseImpl;
import com.glia.widgets.core.permissions.domain.WithCameraPermissionUseCase;
import com.glia.widgets.core.permissions.domain.WithCameraPermissionUseCaseImpl;
import com.glia.widgets.core.permissions.domain.WithReadWritePermissionsUseCase;
import com.glia.widgets.core.permissions.domain.WithReadWritePermissionsUseCaseImpl;
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
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase;
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCaseImpl;
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase;
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCaseImpl;
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase;
import com.glia.widgets.engagement.domain.CurrentOperatorUseCaseImpl;
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase;
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCaseImpl;
import com.glia.widgets.engagement.domain.EndEngagementUseCase;
import com.glia.widgets.engagement.domain.EndEngagementUseCaseImpl;
import com.glia.widgets.engagement.domain.EndScreenSharingUseCase;
import com.glia.widgets.engagement.domain.EndScreenSharingUseCaseImpl;
import com.glia.widgets.engagement.domain.EngagementRequestUseCase;
import com.glia.widgets.engagement.domain.EngagementRequestUseCaseImpl;
import com.glia.widgets.engagement.domain.EngagementStateUseCase;
import com.glia.widgets.engagement.domain.EngagementStateUseCaseImpl;
import com.glia.widgets.engagement.domain.EngagementTypeUseCase;
import com.glia.widgets.engagement.domain.EngagementTypeUseCaseImpl;
import com.glia.widgets.engagement.domain.EnqueueForEngagementUseCase;
import com.glia.widgets.engagement.domain.EnqueueForEngagementUseCaseImpl;
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase;
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCaseImpl;
import com.glia.widgets.engagement.domain.IsOperatorPresentUseCase;
import com.glia.widgets.engagement.domain.IsOperatorPresentUseCaseImpl;
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase;
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCaseImpl;
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase;
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCaseImpl;
import com.glia.widgets.engagement.domain.OperatorMediaUseCase;
import com.glia.widgets.engagement.domain.OperatorMediaUseCaseImpl;
import com.glia.widgets.engagement.domain.OperatorTypingUseCase;
import com.glia.widgets.engagement.domain.OperatorTypingUseCaseImpl;
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase;
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCaseImpl;
import com.glia.widgets.engagement.domain.ScreenSharingUseCase;
import com.glia.widgets.engagement.domain.ScreenSharingUseCaseImpl;
import com.glia.widgets.engagement.domain.SurveyUseCase;
import com.glia.widgets.engagement.domain.SurveyUseCaseImpl;
import com.glia.widgets.engagement.domain.ToggleVisitorAudioMediaStateUseCase;
import com.glia.widgets.engagement.domain.ToggleVisitorAudioMediaStateUseCaseImpl;
import com.glia.widgets.engagement.domain.ToggleVisitorVideoMediaStateUseCase;
import com.glia.widgets.engagement.domain.ToggleVisitorVideoMediaStateUseCaseImpl;
import com.glia.widgets.engagement.domain.VisitorMediaUseCase;
import com.glia.widgets.engagement.domain.VisitorMediaUseCaseImpl;
import com.glia.widgets.filepreview.domain.usecase.DownloadFileUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;
import com.glia.widgets.filepreview.domain.usecase.IsFileReadyForPreviewUseCase;
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase;
import com.glia.widgets.helper.rx.Schedulers;
import com.glia.widgets.push.notifications.IsPushNotificationsSetUpUseCase;
import com.glia.widgets.push.notifications.IsPushNotificationsSetUpUseCaseImpl;
import com.glia.widgets.view.snackbar.LiveObservationPopupUseCase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @hide
 */
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
    private final Dependencies.AuthenticationManagerProvider authenticationManagerProvider;
    private final Schedulers schedulers;
    private final StringProvider stringProvider;
    private final GliaCore gliaCore;
    private final Context applicationContext;
    private Gson gvaGson;

    UseCaseFactory(RepositoryFactory repositoryFactory,
                   PermissionManager permissionManager,
                   PermissionDialogManager permissionDialogManager,
                   INotificationManager notificationManager,
                   GliaSdkConfigurationManager gliaSdkConfigurationManager,
                   ChatHeadManager chatHeadManager,
                   AudioControlManager audioControlManager,
                   Dependencies.AuthenticationManagerProvider authenticationManagerProvider,
                   Schedulers schedulers,
                   StringProvider stringProvider,
                   GliaCore gliaCore,
                   Context applicationContext) {
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
        this.authenticationManagerProvider = authenticationManagerProvider;
        this.applicationContext = applicationContext;
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
        return new HasScreenSharingNotificationChannelEnabledUseCase(applicationContext);
    }

    @NonNull
    public IsShowOverlayPermissionRequestDialogUseCase createIsShowOverlayPermissionRequestDialogUseCase() {
        return new IsShowOverlayPermissionRequestDialogUseCaseImpl(permissionManager, permissionDialogManager, gliaSdkConfigurationManager);
    }

    @NonNull
    public HasCallNotificationChannelEnabledUseCase createHasCallNotificationChannelEnabledUseCase() {
        return new HasCallNotificationChannelEnabledUseCase(applicationContext);
    }

    @NonNull
    public IsShowEnableCallNotificationChannelDialogUseCase createIsShowEnableCallNotificationChannelDialogUseCase() {
        return new IsShowEnableCallNotificationChannelDialogUseCaseImpl(createHasCallNotificationChannelEnabledUseCase(), permissionDialogManager);
    }

    @NonNull
    public SetOverlayPermissionRequestDialogShownUseCase createSetOverlayPermissionRequestDialogShownUseCase() {
        return new SetOverlayPermissionRequestDialogShownUseCaseImpl(permissionDialogManager);
    }

    @NonNull
    public SetEnableCallNotificationChannelDialogShownUseCase createSetEnableCallNotificationChannelDialogShownUseCase() {
        return new SetEnableCallNotificationChannelDialogShownUseCaseImpl(permissionDialogManager);
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
        return new IsAuthenticatedUseCase(authenticationManagerProvider.getAuthenticationManager());
    }

    @NonNull
    public SetEngagementConfigUseCase createSetEngagementConfigUseCase() {
        return new SetEngagementConfigUseCase(repositoryFactory.getEngagementConfigRepository());
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
        return new EndEngagementUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public IsQueueingOrEngagementUseCase getIsQueueingOrEngagementUseCase() {
        return new IsQueueingOrEngagementUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public IsCurrentEngagementCallVisualizerUseCase getIsCurrentEngagementCallVisualizer() {
        return new IsCurrentEngagementCallVisualizerUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public SurveyUseCase getSurveyUseCase() {
        return new SurveyUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EngagementStateUseCase getEngagementStateUseCase() {
        return new EngagementStateUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public ReleaseResourcesUseCase getReleaseResourcesUseCase(DialogContract.Controller dialogController) {
        return new ReleaseResourcesUseCaseImpl(
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
        return new OperatorTypingUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public OperatorMediaUseCase getOperatorMediaUseCase() {
        return new OperatorMediaUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public VisitorMediaUseCase getVisitorMediaUseCase() {
        return new VisitorMediaUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EngagementTypeUseCase getEngagementTypeUseCase() {
        return new EngagementTypeUseCaseImpl(
            getIsQueueingOrEngagementUseCase(),
            getIsCurrentEngagementCallVisualizer(),
            getOperatorMediaUseCase(),
            getVisitorMediaUseCase(),
            getIsOperatorPresentUseCase()
        );
    }

    @NonNull
    public CurrentOperatorUseCase getCurrentOperatorUseCase() {
        return new CurrentOperatorUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public IsOperatorPresentUseCase getIsOperatorPresentUseCase() {
        return new IsOperatorPresentUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public AcceptMediaUpgradeOfferUseCase getAcceptMediaUpgradeOfferUseCase() {
        return new AcceptMediaUpgradeOfferUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public DeclineMediaUpgradeOfferUseCase getDeclineMediaUpgradeOfferUseCase() {
        return new DeclineMediaUpgradeOfferUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EngagementRequestUseCase getEngagementRequestUseCase() {
        return new EngagementRequestUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public ToggleVisitorAudioMediaStateUseCase getToggleVisitorAudioMediaStateUseCase() {
        return new ToggleVisitorAudioMediaStateUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public ToggleVisitorVideoMediaStateUseCase getToggleVisitorVideoMediaStateUseCase() {
        return new ToggleVisitorVideoMediaStateUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public EnqueueForEngagementUseCase getQueueForEngagementUseCase() {
        return new EnqueueForEngagementUseCaseImpl(repositoryFactory.getEngagementRepository());
    }

    @NonNull
    public ScreenSharingUseCase getScreenSharingUseCase() {
        return new ScreenSharingUseCaseImpl(repositoryFactory.getEngagementRepository(), createRemoveScreenSharingNotificationUseCase());
    }

    @NonNull
    public DecideOnQueueingUseCase getDecideOnQueueingUseCase() {
        return new DecideOnQueueingUseCaseImpl(
            createIsShowOverlayPermissionRequestDialogUseCase(),
            createSetOverlayPermissionRequestDialogShownUseCase()
        );
    }

    @NonNull
    public OperatorMediaUpgradeOfferUseCase getOperatorMediaUpgradeOfferUseCase() {
        return new OperatorMediaUpgradeOfferUseCaseImpl(repositoryFactory.getEngagementRepository(), getCurrentOperatorUseCase());
    }

    @NonNull
    public CheckMediaUpgradePermissionsUseCase getCheckMediaUpgradePermissionsUseCase() {
        return new CheckMediaUpgradePermissionsUseCaseImpl(permissionManager);
    }

    @NonNull
    public EndScreenSharingUseCase getEndScreenSharingUseCase() {
        return new EndScreenSharingUseCaseImpl(getScreenSharingUseCase());
    }

    @NonNull
    public IsPushNotificationsSetUpUseCase getIsPushNotificationsSetUpUseCase() {
        return new IsPushNotificationsSetUpUseCaseImpl(applicationContext);
    }

    @NonNull
    public FileProviderUseCase getFileProviderUseCase() {
        return new FileProviderUseCaseImpl(applicationContext);
    }

    @NonNull
    public FixCapturedPictureRotationUseCase getFixCapturedPictureRotationUseCase() {
        return new FixCapturedPictureRotationUseCaseImpl(applicationContext);
    }

    @NonNull
    public UriToFileAttachmentUseCase getUriToFileAttachmentUseCase() {
        return new UriToFileAttachmentUseCaseImpl(applicationContext);
    }

    @NonNull
    public TakePictureUseCase getTakePictureUseCase() {
        return new TakePictureUseCaseImpl(
            applicationContext,
            getFileProviderUseCase(),
            getUriToFileAttachmentUseCase(),
            getFixCapturedPictureRotationUseCase()
        );
    }

    @NonNull
    public WithCameraPermissionUseCase getWithCameraPermissionUseCase() {
        return new WithCameraPermissionUseCaseImpl(permissionManager);
    }

    @NonNull
    public WithReadWritePermissionsUseCase getWithReadWritePermissionsUseCase() {
        return new WithReadWritePermissionsUseCaseImpl(permissionManager);
    }

    @NonNull
    public IsNotificationPermissionGrantedUseCase getIsNotificationPermissionGrantedUseCase() {
        return new IsNotificationPermissionGrantedUseCaseImpl(permissionManager);
    }

    @NonNull
    public RequestNotificationPermissionIfPushNotificationsSetUpUseCase getRequestNotificationPermissionIfPushNotificationsSetUpUseCase() {
        return new RequestNotificationPermissionIfPushNotificationsSetUpUseCaseImpl(
            permissionManager,
            getIsNotificationPermissionGrantedUseCase(),
            getIsPushNotificationsSetUpUseCase()
        );
    }
}
