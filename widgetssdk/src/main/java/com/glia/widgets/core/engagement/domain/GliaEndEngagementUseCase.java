package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;

public class GliaEndEngagementUseCase {

    private final GliaEngagementRepository engagementRepository;

    public GliaEndEngagementUseCase(GliaEngagementRepository engagementRepository) {
        this.engagementRepository = engagementRepository;
    }

    public void execute() {
        engagementRepository.endEngagement();
    }
}
