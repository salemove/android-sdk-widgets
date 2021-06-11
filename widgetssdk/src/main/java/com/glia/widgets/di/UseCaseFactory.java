package com.glia.widgets.di;

import com.glia.widgets.glia.GliaCancelQueueTicketUseCase;
import com.glia.widgets.glia.GliaEndEngagementUseCase;
import com.glia.widgets.glia.GliaLoadHistoryUseCase;
import com.glia.widgets.glia.GliaOnEngagementEndUseCase;
import com.glia.widgets.glia.GliaOnEngagementUseCase;
import com.glia.widgets.glia.GliaOnMessageUseCase;
import com.glia.widgets.glia.GliaOnOperatorMediaStateUseCase;
import com.glia.widgets.glia.GliaOnQueueTicketUseCase;
import com.glia.widgets.glia.GliaOnVisitorMediaStateUseCase;
import com.glia.widgets.glia.GliaQueueForEngagementUseCase;
import com.glia.widgets.glia.GliaQueueForMediaEngagementUseCase;
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

    public UseCaseFactory(RepositoryFactory repositoryFactory,
                          PermissionsManager permissionsManager) {
        this.repositoryFactory = repositoryFactory;
        this.permissionsManager = permissionsManager;
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

    public GliaQueueForEngagementUseCase createQueueForEngagementuseCase() {
        return new GliaQueueForEngagementUseCase(repositoryFactory.getGliaRepository());
    }

    public GliaQueueForMediaEngagementUseCase createGliaQueueForMediaEngagementUseCase() {
        return new GliaQueueForMediaEngagementUseCase(repositoryFactory.getGliaRepository());
    }

    public GliaCancelQueueTicketUseCase createCancelQueueTicketUseCase() {
        return new GliaCancelQueueTicketUseCase(repositoryFactory.getGliaRepository());
    }

    public GliaEndEngagementUseCase createEndEngagementUseCase() {
        return new GliaEndEngagementUseCase(repositoryFactory.getGliaEngagementRepository());
    }

    public GliaOnEngagementUseCase createOnEngagementUseCase() {
        return new GliaOnEngagementUseCase(repositoryFactory.getGliaRepository());
    }

    public GliaOnEngagementEndUseCase createOnEngagementEndUseCase() {
        return new GliaOnEngagementEndUseCase(
                repositoryFactory.getGliaEngagementRepository(),
                createOnEngagementUseCase()
        );
    }

    public GliaOnMessageUseCase createGliaOnMessageUseCase() {
        return new GliaOnMessageUseCase(
                repositoryFactory.getGliaMessageRepository(),
                createOnEngagementUseCase()
        );
    }

    public GliaOnOperatorMediaStateUseCase createGliaOnOperatorMediaStateUseCase() {
        return new GliaOnOperatorMediaStateUseCase(
                createOnEngagementUseCase(),
                repositoryFactory.getGliaMediaStateRepository()
        );
    }

    public GliaSendMessagePreviewUseCase createGliaSendMessagePreviewUseCase() {
        return new GliaSendMessagePreviewUseCase(repositoryFactory.getGliaMessageRepository());
    }

    public GliaSendMessageUseCase createGliaSendMessageUseCase() {
        return new GliaSendMessageUseCase(repositoryFactory.getGliaMessageRepository());
    }

    public GliaOnQueueTicketUseCase createGliaOnQueueTicketUseCase() {
        return new GliaOnQueueTicketUseCase(repositoryFactory.getGliaRepository());
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
                repositoryFactory.getGliaMediaStateRepository()
        );
    }
}
