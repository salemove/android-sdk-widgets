package com.glia.widgets.core.engagement.domain;

import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.RemoveScreenSharingNotificationUseCase;

public class GliaOnEngagementEndUseCase implements
        GliaOnEngagementUseCase.Listener,
        Runnable {

    public interface Listener {
        void engagementEnded();
    }

    private final GliaOnEngagementUseCase engagementUseCase;
    private final RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private final GliaEngagementRepository repository;
    private final GliaQueueRepository gliaQueueRepository;
    private final GliaOperatorMediaRepository operatorMediaRepository;

    private Listener listener;

    public GliaOnEngagementEndUseCase(
            GliaEngagementRepository repository,
            GliaQueueRepository gliaQueueRepository,
            GliaOperatorMediaRepository operatorMediaRepository,
            GliaOnEngagementUseCase engagementUseCase,
            RemoveCallNotificationUseCase removeCallNotificationUseCase,
            RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase
    ) {
        this.repository = repository;
        this.engagementUseCase = engagementUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
        this.removeScreenSharingNotificationUseCase = removeScreenSharingNotificationUseCase;
        this.gliaQueueRepository = gliaQueueRepository;
        this.operatorMediaRepository = operatorMediaRepository;
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
        gliaQueueRepository.cleanOnEngagementEnd();
        operatorMediaRepository.stopListening();
    }
}
