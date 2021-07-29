package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;

public class GliaQueueForChatEngagementUseCase {

    private final GliaEngagementRepository engagementRepository;
    private final GliaQueueRepository repository;

    public GliaQueueForChatEngagementUseCase(
            GliaQueueRepository repository,
            GliaEngagementRepository engagementRepository
    ) {
        this.repository = repository;
        this.engagementRepository = engagementRepository;
    }

    public void execute(String queueId, String contextUrl) {
        if (engagementRepository.hasOngoingEngagement()) {
            repository.onTicketReceived(repository.getQueueTicket());
        } else {
            startQueueing(queueId, contextUrl);
        }
    }

    private void startQueueing(String queueId, String contextUrl) {
        engagementRepository.onChatEngagement();
        repository.startQueueingForEngagement(queueId, contextUrl);
    }
}
