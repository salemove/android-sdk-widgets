package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;

public class GetIsMediaQueueingOngoingUseCase {
    private final GliaQueueRepository repository;

    public GetIsMediaQueueingOngoingUseCase(GliaQueueRepository repository) {
        this.repository = repository;
    }

    public boolean execute() {
        return repository.getIsMediaQueueingOngoing();
    }
}
