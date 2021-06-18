package com.glia.widgets.glia;

import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.model.GliaEngagementRepository;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.RemoveScreenSharingNotificationUseCase;

public class GliaOnEngagementEndUseCase implements
        GliaOnEngagementUseCase.Listener,
        Runnable {

    public interface Listener {
        void engagementEnded();
    }

    private final GliaEngagementRepository repository;
    private final GliaOnEngagementUseCase engagementUseCase;
    private final RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private Listener listener;

    public GliaOnEngagementEndUseCase(
            GliaEngagementRepository repository,
            GliaOnEngagementUseCase engagementUseCase,
            RemoveCallNotificationUseCase removeCallNotificationUseCase,
            RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase
    ) {
        this.repository = repository;
        this.engagementUseCase = engagementUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
        this.removeScreenSharingNotificationUseCase = removeScreenSharingNotificationUseCase;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        engagementUseCase.execute(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            repository.unregisterEngagementEndListener(this);
            engagementUseCase.unregisterListener(this);
            this.listener = null;
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        repository.listenForEngagementEnd(engagement, this);
    }

    @Override
    public void run() {
        if (this.listener != null) {
            listener.engagementEnded();
        }
        removeScreenSharingNotificationUseCase.execute();
        removeCallNotificationUseCase.execute();
    }
}
