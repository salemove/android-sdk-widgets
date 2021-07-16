package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;

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

    public void execute(String queueId, String contextUrl, QueueTicketsEventsListener listener) {
        if (engagementRepository.hasOngoingEngagement()) {
            listener.onTicketReceived(repository.getQueueTicket());
            return;
        } else if (repository.isNoQueueingOngoing()) {
            repository.startQueueingForEngagement(queueId, contextUrl, listener);
        } else {
            repository.addOngoingQueueingEventListener(listener);
        }
        engagementRepository.onChatEngagement();
    }
}
