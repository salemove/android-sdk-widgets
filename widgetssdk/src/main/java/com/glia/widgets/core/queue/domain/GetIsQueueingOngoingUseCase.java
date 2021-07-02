package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;

public class GetIsQueueingOngoingUseCase {
    private final GliaQueueRepository repository;

    public GetIsQueueingOngoingUseCase(GliaQueueRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return !repository.isNoQueueingOngoing();
    }
}
