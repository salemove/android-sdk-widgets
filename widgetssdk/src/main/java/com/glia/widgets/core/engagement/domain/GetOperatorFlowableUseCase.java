package com.glia.widgets.core.engagement.domain;

import com.glia.androidsdk.Operator;
import com.glia.widgets.core.engagement.GliaEngagementStateRepository;

import io.reactivex.Flowable;

public class GetOperatorFlowableUseCase {
    private final GliaEngagementStateRepository gliaEngagementStateRepository;

    public GetOperatorFlowableUseCase(GliaEngagementStateRepository gliaEngagementStateRepository) {
        this.gliaEngagementStateRepository = gliaEngagementStateRepository;
    }

    public Flowable<Operator> execute() {
        return gliaEngagementStateRepository.operatorFlowable();
    }
}
