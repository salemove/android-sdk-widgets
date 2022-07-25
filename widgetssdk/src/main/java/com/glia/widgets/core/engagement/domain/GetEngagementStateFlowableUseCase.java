package com.glia.widgets.core.engagement.domain;

import com.glia.widgets.core.engagement.GliaEngagementStateRepository;
import com.glia.widgets.core.engagement.domain.model.EngagementStateEvent;

import io.reactivex.Flowable;

public class GetEngagementStateFlowableUseCase {

    private final GliaEngagementStateRepository engagementStateRepository;

    public GetEngagementStateFlowableUseCase(GliaEngagementStateRepository engagementStateRepository) {
        this.engagementStateRepository = engagementStateRepository;
    }

    public Flowable<EngagementStateEvent> execute() {
        return engagementStateRepository.engagementStateEventFlowable();
    }
}
