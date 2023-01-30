package com.glia.widgets.core.engagement.domain;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.GliaEngagementStateRepository;
import com.glia.widgets.core.engagement.GliaEngagementTypeRepository;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.survey.GliaSurveyRepository;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;

public class GliaOnEngagementEndUseCase implements
        GliaOnEngagementUseCase.Listener {

    public interface Listener {
        void engagementEnded();
    }

    private final GliaOnEngagementUseCase engagementUseCase;
    private final RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private final GliaEngagementRepository repository;
    private final GliaOperatorMediaRepository operatorMediaRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final GliaSurveyRepository surveyRepository;
    private final GliaVisitorMediaRepository gliaVisitorMediaRepository;
    private final GliaEngagementTypeRepository gliaEngagementTypeRepository;

    private Listener listener;

    private class EndEngagementRunnable implements Runnable {
        private final Engagement engagement;

        public EndEngagementRunnable(Engagement engagement) {
            this.engagement = engagement;
        }

        @Override
        public void run() {
            surveyRepository.onEngagementEnded(engagement);
            if (listener != null) {
                listener.engagementEnded();
            }
            operatorMediaRepository.stopListening(engagement);
            fileAttachmentRepository.clearObservers();
            fileAttachmentRepository.detachAllFiles();
            removeScreenSharingNotificationUseCase.execute();
            removeCallNotificationUseCase.execute();
            gliaVisitorMediaRepository.onEngagementEnded(engagement);
            gliaEngagementTypeRepository.setIsSecureEngagement(false);
        }
    }

    private EndEngagementRunnable endEngagementRunnable = null;

    public GliaOnEngagementEndUseCase(
            GliaEngagementRepository repository,
            GliaOperatorMediaRepository operatorMediaRepository,
            FileAttachmentRepository fileAttachmentRepository,
            GliaOnEngagementUseCase engagementUseCase,
            RemoveCallNotificationUseCase removeCallNotificationUseCase,
            RemoveScreenSharingNotificationUseCase removeScreenSharingNotificationUseCase,
            GliaSurveyRepository surveyRepository,
            GliaVisitorMediaRepository gliaVisitorMediaRepository,
            GliaEngagementTypeRepository gliaEngagementTypeRepository
    ) {
        this.repository = repository;
        this.engagementUseCase = engagementUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
        this.removeScreenSharingNotificationUseCase = removeScreenSharingNotificationUseCase;
        this.operatorMediaRepository = operatorMediaRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.surveyRepository = surveyRepository;
        this.gliaVisitorMediaRepository = gliaVisitorMediaRepository;
        this.gliaEngagementTypeRepository = gliaEngagementTypeRepository;
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
