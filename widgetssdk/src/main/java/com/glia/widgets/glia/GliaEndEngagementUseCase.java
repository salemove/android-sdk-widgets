package com.glia.widgets.glia;

import com.glia.widgets.model.GliaEngagementRepository;

public class GliaEndEngagementUseCase {

    private final GliaEngagementRepository engagementRepository;

    public GliaEndEngagementUseCase(GliaEngagementRepository engagementRepository) {
        this.engagementRepository = engagementRepository;
    }

    public void execute(){
        engagementRepository.endEngagement();
    }
}
