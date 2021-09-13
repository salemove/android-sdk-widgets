package com.glia.widgets.core.engagement.domain;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase;

public class GliaOnEngagementEndUseCase implements
        GliaOnEngagementUseCase.Listener {

    public interface Listener {
        void engagementEnded();
    }

    private final GliaOnEngagementUseCase engagementUseCase;
    private final RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private final GliaEngagementRepository repository;
    private final GliaQueueRepository gliaQueueRepository;
    private final GliaOperatorMediaRepository operatorMediaRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    private Listener listener;

    private class EndEngagementRunnable implements Runnable {
        private final Engagement engagement;

        public EndEngagementRunnable(Engagement engagement) {
            this.engagement = engagement;
        }

        @Override
        public void run() {
            if (listener != null) {
                listener.engagementEnded();
            }
            gliaQueueRepository.cleanOnEngagementEnd();
            operatorMediaRepository.stopListening(engagement);
            repository.clearEngagementType();
            fileAttachmentRepository.clearObservers();
            removeScreenSharingNotificationUseCase.execute();
            removeCallNotificationUseCase.execute();
        }
    }

    private EndEngagementRunnable endEngagementRunnable = null;

    public GliaOnEngagementEndUseCase(
            GliaEngagementRepository repository,
            GliaQueueRepository gliaQueueRepository,
            GliaOperatorMediaRepository operatorMediaRepository,
            FileAttachmentRepository fileAttachmentRepository,
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
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        engagementUseCase.execute(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            repository.unregisterEngagementEndListener(endEngagementRunnable);
            engagementUseCase.unregisterListener(this);
            endEngagementRunnable = null;
            this.listener = null;
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        endEngagementRunnable = new EndEngagementRunnable(engagement);
        repository.listenForEngagementEnd(engagement, endEngagementRunnable);
    }
}
