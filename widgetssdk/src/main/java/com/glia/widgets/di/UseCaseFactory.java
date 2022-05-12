package com.glia.widgets.di;

import com.glia.widgets.call.domain.ToggleVisitorAudioMediaMuteUseCase;
import com.glia.widgets.call.domain.ToggleVisitorVideoUseCase;
import com.glia.widgets.chat.domain.GliaLoadHistoryUseCase;
import com.glia.widgets.chat.domain.GliaOnMessageUseCase;
import com.glia.widgets.chat.domain.GliaOnOperatorTypingUseCase;
import com.glia.widgets.chat.domain.GliaSendMessagePreviewUseCase;
import com.glia.widgets.chat.domain.GliaSendMessageUseCase;
import com.glia.widgets.chat.domain.IsEnableChatEditTextUseCase;
import com.glia.widgets.chat.domain.IsShowSendButtonUseCase;
import com.glia.widgets.chat.domain.SiteInfoUseCase;
import com.glia.widgets.core.chathead.ChatHeadManager;
import com.glia.widgets.core.chathead.domain.IsDisplayApplicationChatHeadUseCase;
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase;
import com.glia.widgets.core.chathead.domain.ToggleChatHeadServiceUseCase;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.PermissionDialogManager;
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.dialog.domain.SetEnableCallNotificationChannelDialogShownUseCase;
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.engagement.domain.OnUpgradeToMediaEngagementUseCase;
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileAttachmentsObserverUseCase;
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.core.fileupload.domain.GetFileAttachmentsUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentObserverUseCase;
import com.glia.widgets.core.fileupload.domain.RemoveFileAttachmentUseCase;
import com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase;
import com.glia.widgets.core.notification.device.INotificationManager;
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase;
import com.glia.widgets.core.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.permissions.PermissionManager;
import com.glia.widgets.core.permissions.domain.HasCallNotificationChannelEnabledUseCase;
import com.glia.widgets.core.permissions.domain.HasOverlayEnabledUseCase;
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase;
import com.glia.widgets.core.queue.domain.GetIsMediaQueueingOngoingUseCase;
import com.glia.widgets.core.queue.domain.GetIsQueueingOngoingUseCase;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForMediaEngagementUseCase;
import com.glia.widgets.core.queue.domain.SubscribeToQueueingStateChangeUseCase;
import com.glia.widgets.core.queue.domain.UnsubscribeFromQueueingStateChangeUseCase;
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
    private static ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private static ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private static RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private static ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private static RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private static ToggleChatHeadServiceUseCase toggleChatHeadServiceUseCase;
    private static IsDisplayApplicationChatHeadUseCase isDisplayApplicationChatHeadUseCase;
    private static ResolveChatHeadNavigationUseCase resolveChatHeadNavigationUseCase;

    private static GliaQueueForChatEngagementUseCase gliaQueueForChatEngagementUseCase;
    private static GliaQueueForMediaEngagementUseCase gliaQueueForMediaEngagementUseCase;
    private final RepositoryFactory repositoryFactory;
    private final PermissionManager permissionManager;
    private final PermissionDialogManager permissionDialogManager;
    private final GliaSdkConfigurationManager gliaSdkConfigurationManager;
    private final INotificationManager notificationManager;
    private final ChatHeadManager chatHeadManager;
    private final Schedulers schedulers;

    public UseCaseFactory(RepositoryFactory repositoryFactory,
                          PermissionManager permissionManager,
                          PermissionDialogManager permissionDialogManager,
                          INotificationManager notificationManager,
                          GliaSdkConfigurationManager gliaSdkConfigurationManager,
                          ChatHeadManager chatHeadManager,
                          Schedulers schedulers
    ) {
        this.repositoryFactory = repositoryFactory;
        this.permissionManager = permissionManager;
        this.permissionDialogManager = permissionDialogManager;
        this.notificationManager = notificationManager;
        this.gliaSdkConfigurationManager = gliaSdkConfigurationManager;
        this.chatHeadManager = chatHeadManager;
        this.schedulers = schedulers;
    }

    public ToggleChatHeadServiceUseCase getToggleChatHeadServiceUseCase() {
        if (toggleChatHeadServiceUseCase == null) {
            toggleChatHeadServiceUseCase = new ToggleChatHeadServiceUseCase(
                    repositoryFactory.getGliaEngagementRepository(),
                    repositoryFactory.getGliaQueueRepository(),
                    chatHeadManager,
                    permissionManager,
                    gliaSdkConfigurationManager
            );
        }
        return toggleChatHeadServiceUseCase;
    }

    public IsDisplayApplicationChatHeadUseCase getIsDisplayApplicationChatHeadUseCase() {
        if (isDisplayApplicationChatHeadUseCase == null) {
            isDisplayApplicationChatHeadUseCase = new IsDisplayApplicationChatHeadUseCase(
                    repositoryFactory.getGliaEngagementRepository(),
                    repositoryFactory.getGliaQueueRepository(),
                    permissionManager,
                    gliaSdkConfigurationManager
            );
        }
        return isDisplayApplicationChatHeadUseCase;
    }

    public ResolveChatHeadNavigationUseCase getResolveChatHeadNavigationUseCase() {
        if (resolveChatHeadNavigationUseCase == null) {
            resolveChatHeadNavigationUseCase = new ResolveChatHeadNavigationUseCase(
                    repositoryFactory.getGliaEngagementRepository(),
                    repositoryFactory.getGliaQueueRepository()
            );
        }
        return resolveChatHeadNavigationUseCase;
    }

    public ShowAudioCallNotificationUseCase createShowAudioCallNotificationUseCase() {
        if (showAudioCallNotificationUseCase == null)
            showAudioCallNotificationUseCase = new ShowAudioCallNotificationUseCase(notificationManager);
        return showAudioCallNotificationUseCase;
    }

    public ShowVideoCallNotificationUseCase createShowVideoCallNotificationUseCase() {
        if (showVideoCallNotificationUseCase == null)
            showVideoCallNotificationUseCase = new ShowVideoCallNotificationUseCase(notificationManager);
        return showVideoCallNotificationUseCase;
    }

    public RemoveCallNotificationUseCase createRemoveCallNotificationUseCase() {
        if (removeCallNotificationUseCase == null)
            removeCallNotificationUseCase = new RemoveCallNotificationUseCase(notificationManager);
        return removeCallNotificationUseCase;
    }

    public ShowScreenSharingNotificationUseCase createShowScreenSharingNotificationUseCase() {
        if (showScreenSharingNotificationUseCase == null)
            showScreenSharingNotificationUseCase = new ShowScreenSharingNotificationUseCase(notificationManager);
        return showScreenSharingNotificationUseCase;
    }

    public RemoveScreenSharingNotificationUseCase createRemoveScreenSharingNotificationUseCase() {
        if (removeScreenSharingNotificationUseCase == null)
            removeScreenSharingNotificationUseCase = new RemoveScreenSharingNotificationUseCase(notificationManager);
        return removeScreenSharingNotificationUseCase;
    }

    public GliaLoadHistoryUseCase createGliaLoadHistoryUseCase() {
        return new GliaLoadHistoryUseCase(repositoryFactory.getGliaMessageRepository());
    }

    public GliaQueueForChatEngagementUseCase createQueueForChatEngagementUseCase() {
        if (gliaQueueForChatEngagementUseCase == null) {
            gliaQueueForChatEngagementUseCase = new GliaQueueForChatEngagementUseCase(
                    repositoryFactory.getGliaQueueRepository(),
                    repositoryFactory.getGliaEngagementRepository()
            );
        }
        return gliaQueueForChatEngagementUseCase;
    }

    public GliaQueueForMediaEngagementUseCase createQueueForMediaEngagementUseCase() {
        if (gliaQueueForMediaEngagementUseCase == null) {
            gliaQueueForMediaEngagementUseCase = new GliaQueueForMediaEngagementUseCase(
                    repositoryFactory.getGliaQueueRepository(),
                    repositoryFactory.getGliaEngagementRepository()
            );
        }
        return gliaQueueForMediaEngagementUseCase;
    }

    public GliaCancelQueueTicketUseCase createCancelQueueTicketUseCase() {
        return new GliaCancelQueueTicketUseCase(
                repositoryFactory.getGliaQueueRepository(),
                repositoryFactory.getGliaEngagementRepository()
        );
    }

    public GliaEndEngagementUseCase createEndEngagementUseCase() {
        return new GliaEndEngagementUseCase(repositoryFactory.getGliaEngagementRepository());
    }

    public GliaOnEngagementUseCase createOnEngagementUseCase() {
        return new GliaOnEngagementUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                repositoryFactory.getGliaQueueRepository(),
                repositoryFactory.getGliaVisitorMediaRepository()
        );
    }

    public GliaOnEngagementEndUseCase createOnEngagementEndUseCase() {
        return new GliaOnEngagementEndUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaQueueRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                repositoryFactory.getGliaFileAttachmentRepository(),
                createOnEngagementUseCase(),
                createRemoveCallNotificationUseCase(),
                createRemoveScreenSharingNotificationUseCase(),
                repositoryFactory.getGliaSurveyRepository(),
                repositoryFactory.getGliaVisitorMediaRepository()
        );
    }

    public GliaOnMessageUseCase createGliaOnMessageUseCase() {
        return new GliaOnMessageUseCase(
                repositoryFactory.getGliaMessageRepository(),
                createOnEngagementUseCase()
        );
    }

    public GliaOnOperatorTypingUseCase createGliaOnOperatorTypingUseCase() {
        return new GliaOnOperatorTypingUseCase(
                repositoryFactory.getGliaMessageRepository(),
                createOnEngagementUseCase()
        );
    }

    public GliaSendMessagePreviewUseCase createGliaSendMessagePreviewUseCase() {
        return new GliaSendMessagePreviewUseCase(repositoryFactory.getGliaMessageRepository());
    }

    public GliaSendMessageUseCase createGliaSendMessageUseCase() {
        return new GliaSendMessageUseCase(
                repositoryFactory.getGliaMessageRepository(),
                repositoryFactory.getGliaFileAttachmentRepository(),
                repositoryFactory.getGliaEngagementRepository()
        );
    }

    public GetIsMediaQueueingOngoingUseCase createGetIsMediaEngagementOngoingUseCase() {
        return new GetIsMediaQueueingOngoingUseCase(
                repositoryFactory.getGliaQueueRepository()
        );
    }

    public GetIsQueueingOngoingUseCase createGetIsQueueingOngoingUseCase() {
        return new GetIsQueueingOngoingUseCase(
                repositoryFactory.getGliaQueueRepository()
        );
    }

    public AddOperatorMediaStateListenerUseCase createAddOperatorMediaStateListenerUseCase() {
        return new AddOperatorMediaStateListenerUseCase(
                repositoryFactory.getGliaOperatorMediaRepository()
        );
    }

    public OnUpgradeToMediaEngagementUseCase createOnUpgradeToMediaEngagementUseCase() {
        return new OnUpgradeToMediaEngagementUseCase(
                repositoryFactory.getGliaEngagementRepository()
        );
    }

    public ShouldShowMediaEngagementViewUseCase createShouldShowMediaEngagementViewUseCase() {
        return new ShouldShowMediaEngagementViewUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                repositoryFactory.getGliaQueueRepository()
        );
    }

    public AddFileAttachmentsObserverUseCase createAddFileAttachmentsObserverUseCase() {
        return new AddFileAttachmentsObserverUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    public AddFileToAttachmentAndUploadUseCase createAddFileToAttachmentAndUploadUseCase() {
        return new AddFileToAttachmentAndUploadUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaFileAttachmentRepository()
        );
    }

    public GetFileAttachmentsUseCase createGetFileAttachmentsUseCase() {
        return new GetFileAttachmentsUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    public RemoveFileAttachmentObserverUseCase createRemoveFileAttachmentObserverUseCase() {
        return new RemoveFileAttachmentObserverUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    public RemoveFileAttachmentUseCase createRemoveFileAttachmentUseCase() {
        return new RemoveFileAttachmentUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    public SupportedFileCountCheckUseCase createSupportedFileCountCheckUseCase() {
        return new SupportedFileCountCheckUseCase(repositoryFactory.getGliaFileAttachmentRepository());
    }

    public IsShowSendButtonUseCase createIsShowSendButtonUseCase() {
        return new IsShowSendButtonUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaFileAttachmentRepository()
        );
    }

    public HasOverlayEnabledUseCase createHasOverlayEnabledUseCase() {
        return new HasOverlayEnabledUseCase(permissionManager);
    }

    public HasScreenSharingNotificationChannelEnabledUseCase createHasScreenSharingNotificationChannelEnabledUseCase() {
        return new HasScreenSharingNotificationChannelEnabledUseCase(permissionManager);
    }

    public IsShowOverlayPermissionRequestDialogUseCase createIsShowOverlayPermissionRequestDialogUseCase() {
        return new IsShowOverlayPermissionRequestDialogUseCase(permissionManager, permissionDialogManager, gliaSdkConfigurationManager);
    }

    public HasCallNotificationChannelEnabledUseCase createHasCallNotificationChannelEnabledUseCase() {
        return new HasCallNotificationChannelEnabledUseCase(permissionManager);
    }

    public IsShowEnableCallNotificationChannelDialogUseCase createIsShowEnableCallNotificationChannelDialogUseCase() {
        return new IsShowEnableCallNotificationChannelDialogUseCase(permissionManager, permissionDialogManager);
    }

    public SetOverlayPermissionRequestDialogShownUseCase createSetOverlayPermissionRequestDialogShownUseCase() {
        return new SetOverlayPermissionRequestDialogShownUseCase(permissionDialogManager);
    }

    public SetEnableCallNotificationChannelDialogShownUseCase createSetEnableCallNotificationChannelDialogShownUseCase() {
        return new SetEnableCallNotificationChannelDialogShownUseCase(permissionDialogManager);
    }

    public GetImageFileFromDownloadsUseCase createGetImageFileFromDownloadsUseCase() {
        return new GetImageFileFromDownloadsUseCase(repositoryFactory.getGliaFileRepository());
    }

    public GetImageFileFromCacheUseCase createGetImageFileFromCacheUseCase() {
        return new GetImageFileFromCacheUseCase(repositoryFactory.getGliaFileRepository());
    }

    public GetImageFileFromNetworkUseCase createGetImageFileFromNetworkUseCase() {
        return new GetImageFileFromNetworkUseCase(repositoryFactory.getGliaFileRepository());
    }

    public PutImageFileToDownloadsUseCase createPutImageFileToDownloadsUseCase() {
        return new PutImageFileToDownloadsUseCase(repositoryFactory.getGliaFileRepository());
    }

    public DownloadFileUseCase createDownloadFileUseCase() {
        return new DownloadFileUseCase(repositoryFactory.getGliaFileRepository());
    }

    public IsEnableChatEditTextUseCase createIsEnableChatEditTextUseCase() {
        return new IsEnableChatEditTextUseCase();
    }

    public SubscribeToQueueingStateChangeUseCase createSubscribeToQueueingStateChangeUseCase() {
        return new SubscribeToQueueingStateChangeUseCase(repositoryFactory.getGliaQueueRepository());
    }

    public UnsubscribeFromQueueingStateChangeUseCase createUnSubscribeToQueueingStateChangeUseCase() {
        return new UnsubscribeFromQueueingStateChangeUseCase(repositoryFactory.getGliaQueueRepository());
    }

    public SiteInfoUseCase createSiteInfoUseCase() {
        return new SiteInfoUseCase(repositoryFactory.getGliaEngagementRepository());
    }

    public GliaSurveyUseCase getGliaSurveyUseCase() {
        return new GliaSurveyUseCase(repositoryFactory.getGliaSurveyRepository());
    }

    public GliaSurveyAnswerUseCase getSurveyAnswerUseCase() {
        return new GliaSurveyAnswerUseCase(repositoryFactory.getGliaSurveyRepository());
    }

    public AddVisitorMediaStateListenerUseCase createAddVisitorMediaStateListenerUseCase() {
        return new AddVisitorMediaStateListenerUseCase(repositoryFactory.getGliaVisitorMediaRepository());
    }

    public RemoveVisitorMediaStateListenerUseCase createRemoveVisitorMediaStateListenerUseCase() {
        return new RemoveVisitorMediaStateListenerUseCase(repositoryFactory.getGliaVisitorMediaRepository());
    }

    public ToggleVisitorAudioMediaMuteUseCase createToggleVisitorAudioMediaMuteUseCase() {
        return new ToggleVisitorAudioMediaMuteUseCase(
                schedulers,
                repositoryFactory.getGliaVisitorMediaRepository()
        );
    }

    public ToggleVisitorVideoUseCase createToggleVisitorVideoUseCase() {
        return new ToggleVisitorVideoUseCase(
                schedulers,
                repositoryFactory.getGliaVisitorMediaRepository()
        );
    }

    public IsShowVideoUseCase createIsShowVideoUseCase() {
        return new IsShowVideoUseCase(schedulers);
    }

    public IsShowOnHoldUseCase createIsShowOnHoldUseCase() {
        return new IsShowOnHoldUseCase(schedulers);
    }
}
