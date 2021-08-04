package com.glia.widgets.core.queue.domain;

import com.glia.androidsdk.Engagement;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;

public class GliaQueueForMediaEngagementUseCase {

    private final GliaQueueRepository repository;
    private final GliaEngagementRepository engagementRepository;

    public GliaQueueForMediaEngagementUseCase(
            GliaQueueRepository repository,
            GliaEngagementRepository engagementRepository
    ) {
        this.repository = repository;
        this.engagementRepository = engagementRepository;
    }

    public void execute(
            String queueId,
            String contextUrl,
            Engagement.MediaType mediaType,
            QueueTicketsEventsListener listener
    ) {
        if (engagementRepository.hasOngoingEngagement()) {
            listener.onTicketReceived(repository.getQueueTicket());
            return;
        } else if (repository.isNoQueueingOngoing()) {
            repository.startQueueingForMediaEngagement(queueId, contextUrl, mediaType, listener);
        } else {
            repository.addOngoingQueueingEventListener(listener);
        }
        engagementRepository.onMediaEngagement();
    }
}
