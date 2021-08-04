package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;

public class GliaCancelQueueTicketUseCase {

    private final GliaQueueRepository repository;
    private final GliaEngagementRepository engagementRepository;

    public GliaCancelQueueTicketUseCase(GliaQueueRepository repository, GliaEngagementRepository engagementRepository) {
        this.repository = repository;
        this.engagementRepository = engagementRepository;
    }

    public void execute() {
        repository.cancelTicket();
        engagementRepository.clearEngagementType();
    }
}
