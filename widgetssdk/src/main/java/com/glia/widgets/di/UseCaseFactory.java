package com.glia.widgets.di;

import com.glia.widgets.chat.domain.IsShowSendButtonUseCase;
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase;
import com.glia.widgets.core.engagement.domain.OnUpgradeToMediaEngagementUseCase;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.queue.domain.AddQueueTicketsEventsListenerUseCase;
import com.glia.widgets.core.queue.domain.GetIsMediaQueueingOngoingUseCase;
import com.glia.widgets.core.queue.domain.GetIsQueueingOngoingUseCase;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForChatEngagementUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForMediaEngagementUseCase;
import com.glia.widgets.fileupload.domain.AddFileAttachmentsObserverUseCase;
import com.glia.widgets.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.fileupload.domain.GetFileAttachmentsUseCase;
import com.glia.widgets.fileupload.domain.RemoveFileAttachmentObserverUseCase;
import com.glia.widgets.fileupload.domain.RemoveFileAttachmentUseCase;
import com.glia.widgets.glia.GliaLoadHistoryUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.glia.GliaOnMessageUseCase;
import com.glia.widgets.core.visitor.domain.GliaOnVisitorMediaStateUseCase;
import com.glia.widgets.glia.GliaSendMessagePreviewUseCase;
import com.glia.widgets.glia.GliaSendMessageUseCase;
import com.glia.widgets.model.PermissionsManager;
import com.glia.widgets.notification.device.INotificationManager;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowScreenSharingNotificationUseCase;
import com.glia.widgets.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.permissions.CheckIfHasPermissionsUseCase;
import com.glia.widgets.permissions.CheckIfShowPermissionsDialogUseCase;
import com.glia.widgets.permissions.ResetPermissionsUseCase;
import com.glia.widgets.permissions.UpdateDialogShownUseCase;
import com.glia.widgets.permissions.UpdatePermissionsUseCase;

public class UseCaseFactory {
    private static ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private static ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private static RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private static ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private static RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;

    private final RepositoryFactory repositoryFactory;
    private final PermissionsManager permissionsManager;
    private final INotificationManager notificationManager;

    public UseCaseFactory(RepositoryFactory repositoryFactory,
                          PermissionsManager permissionsManager,
                          INotificationManager notificationManager) {
        this.repositoryFactory = repositoryFactory;
        this.permissionsManager = permissionsManager;
        this.notificationManager = notificationManager;
    }

    public static ShowAudioCallNotificationUseCase createShowAudioCallNotificationUseCase(INotificationManager notificationManager) {
        if (showAudioCallNotificationUseCase == null)
            showAudioCallNotificationUseCase = new ShowAudioCallNotificationUseCase(notificationManager);
        return showAudioCallNotificationUseCase;
    }

    public static ShowVideoCallNotificationUseCase createShowVideoCallNotificationUseCase(INotificationManager notificationManager) {
        if (showVideoCallNotificationUseCase == null)
            showVideoCallNotificationUseCase = new ShowVideoCallNotificationUseCase(notificationManager);
        return showVideoCallNotificationUseCase;
    }

    public static RemoveCallNotificationUseCase createRemoveCallNotificationUseCase(INotificationManager notificationManager) {
        if (removeCallNotificationUseCase == null)
            removeCallNotificationUseCase = new RemoveCallNotificationUseCase(notificationManager);
        return removeCallNotificationUseCase;
    }

    public static ShowScreenSharingNotificationUseCase createShowScreenSharingNotificationUseCase(INotificationManager notificationManager) {
        if (showScreenSharingNotificationUseCase == null)
            showScreenSharingNotificationUseCase = new ShowScreenSharingNotificationUseCase(notificationManager);
        return showScreenSharingNotificationUseCase;
    }

    public static RemoveScreenSharingNotificationUseCase createRemoveScreenSharingNotificationUseCase(INotificationManager notificationManager) {
        if (removeScreenSharingNotificationUseCase == null)
            removeScreenSharingNotificationUseCase = new RemoveScreenSharingNotificationUseCase(notificationManager);
        return removeScreenSharingNotificationUseCase;
    }

    public GliaLoadHistoryUseCase createGliaLoadHistoryUseCase() {
        return new GliaLoadHistoryUseCase(repositoryFactory.getGliaMessageRepository());
    }

    private static GliaQueueForChatEngagementUseCase gliaQueueForChatEngagementUseCase;
    private static GliaQueueForMediaEngagementUseCase gliaQueueForMediaEngagementUseCase;

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
                repositoryFactory.getGliaQueueRepository()
        );
    }

    public GliaOnEngagementEndUseCase createOnEngagementEndUseCase() {
        return new GliaOnEngagementEndUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaQueueRepository(),
                repositoryFactory.getGliaOperatorMediaRepository(),
                createOnEngagementUseCase(),
                createRemoveCallNotificationUseCase(notificationManager),
                createRemoveScreenSharingNotificationUseCase(notificationManager)
        );
    }

    public GliaOnMessageUseCase createGliaOnMessageUseCase() {
        return new GliaOnMessageUseCase(
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

    public CheckIfShowPermissionsDialogUseCase createCheckIfShowPermissionsDialogUseCase() {
        return new CheckIfShowPermissionsDialogUseCase(permissionsManager);
    }

    public UpdateDialogShownUseCase createUpdateDialogShownUseCase() {
        return new UpdateDialogShownUseCase(permissionsManager);
    }

    public UpdatePermissionsUseCase createUpdatePermissionsUseCase() {
        return new UpdatePermissionsUseCase(permissionsManager);
    }

    public ResetPermissionsUseCase createResetPermissionsUseCase() {
        return new ResetPermissionsUseCase(permissionsManager);
    }

    public CheckIfHasPermissionsUseCase createCheckIfHasPermissionsUseCase() {
        return new CheckIfHasPermissionsUseCase(permissionsManager);
    }

    public GliaOnVisitorMediaStateUseCase createGliaOnVisitorMediaStateUseCase() {
        return new GliaOnVisitorMediaStateUseCase(
                createOnEngagementUseCase(),
                repositoryFactory.getGliaVisitorMediaRepository()
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

    public AddQueueTicketsEventsListenerUseCase createAddQueueTicketsEventsListenerUseCase() {
        return new AddQueueTicketsEventsListenerUseCase(
                repositoryFactory.getGliaQueueRepository()
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
        return new AddFileToAttachmentAndUploadUseCase(repositoryFactory.getGliaFileAttachmentRepository());
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

    public IsShowSendButtonUseCase createIsShowSendButtonUseCase() {
        return new IsShowSendButtonUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                repositoryFactory.getGliaFileAttachmentRepository()
        );
    }
}
