package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;

public class GliaCancelQueueTicketUseCase {

    private final GliaQueueRepository repository;

    public GliaCancelQueueTicketUseCase(GliaQueueRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.cancelTicket();
    }
}
