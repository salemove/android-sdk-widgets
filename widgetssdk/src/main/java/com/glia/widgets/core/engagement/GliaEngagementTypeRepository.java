package com.glia.widgets.core.engagement;

import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;

public class GliaEngagementTypeRepository {
    private final GliaEngagementRepository engagementRepository;
    private final GliaVisitorMediaRepository visitorMediaRepository;
    private final GliaOperatorMediaRepository operatorMediaRepository;
    private final GliaEngagementStateRepository engagementStateRepository;
    private boolean isSecureEngagement = false;

    public GliaEngagementTypeRepository(
            GliaEngagementRepository engagementRepository,
            GliaVisitorMediaRepository visitorMediaRepository,
            GliaOperatorMediaRepository operatorMediaRepository,
            GliaEngagementStateRepository engagementStateRepository
    ) {
        this.engagementRepository = engagementRepository;
        this.visitorMediaRepository = visitorMediaRepository;
        this.operatorMediaRepository = operatorMediaRepository;
        this.engagementStateRepository = engagementStateRepository;
    }

    public boolean isMediaEngagement() {
        return engagementRepository.hasOngoingEngagement() &&
                engagementStateRepository.isOperatorPresent() &&
                hasAnyMedia();
    }

    public boolean isChatEngagement() {
        return engagementRepository.hasOngoingEngagement() &&
                !engagementRepository.isCallVisualizerEngagement() &&
                engagementStateRepository.isOperatorPresent() &&
                !hasAnyMedia();
    }

    private boolean hasAnyMedia() {
        return hasVisitorMedia() ||
                operatorMediaRepository.hasOperatorMedia();
    }

    private boolean hasVisitorMedia() {
        return visitorMediaRepository.hasVisitorVideoMedia().blockingGet() ||
                visitorMediaRepository.hasVisitorAudioMedia().blockingGet();
    }

    public boolean isSecureEngagement() {
        return isSecureEngagement;
    }

    public void setIsSecureEngagement(boolean secureEngagement) {
        isSecureEngagement = secureEngagement;
    }
}
