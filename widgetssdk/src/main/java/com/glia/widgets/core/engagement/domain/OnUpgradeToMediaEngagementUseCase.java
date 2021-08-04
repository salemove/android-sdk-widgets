package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;

public class OnUpgradeToMediaEngagementUseCase {
    private final GliaEngagementRepository engagementRepository;

    public OnUpgradeToMediaEngagementUseCase(GliaEngagementRepository engagementRepository) {
        this.engagementRepository = engagementRepository;
    }

    public void execute() {
        engagementRepository.onUpgradeToMediaEngagement();
    }
}
