package com.glia.widgets.di;

import com.glia.widgets.glia.GliaLoadHistoryUseCase;
import com.glia.widgets.glia.GliaQueueForEngagementUseCase;
import com.glia.widgets.model.GliaMessageRepository;
import com.glia.widgets.model.GliaTicketRepository;
import com.glia.widgets.notification.device.INotificationManager;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowScreenSharingNotificationUseCase;
import com.glia.widgets.notification.domain.ShowVideoCallNotificationUseCase;

public class UseCaseFactory {
    private static ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private static ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private static RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private static ShowScreenSharingNotificationUseCase showScreenSharingNotificationUseCase;
    private static RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private static GliaLoadHistoryUseCase loadHistoryUseCase;
    private static GliaQueueForEngagementUseCase queueForEngagementUseCase;

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

    public static GliaLoadHistoryUseCase createCoreLoadHistoryUseCase(GliaMessageRepository coreRepository) {
        if (loadHistoryUseCase == null) {
            loadHistoryUseCase = new GliaLoadHistoryUseCase(coreRepository);
        }
        return loadHistoryUseCase;
    }

    public static GliaQueueForEngagementUseCase createQueueForEngagementuseCase(GliaTicketRepository ticketRepository) {
        if (queueForEngagementUseCase == null) {
            queueForEngagementUseCase = new GliaQueueForEngagementUseCase(ticketRepository);
        }
        return queueForEngagementUseCase;
    }
}
