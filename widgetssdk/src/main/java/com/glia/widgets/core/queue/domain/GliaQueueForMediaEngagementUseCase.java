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
            Engagement.MediaType mediaType
    ) {
        if (engagementRepository.hasOngoingEngagement()) {
            repository.onTicketReceived(repository.getQueueTicket());
        } else {
            startQueueing(queueId, contextUrl, mediaType);
        }
    }

    private void startQueueing(String queueId,
                               String contextUrl,
                               Engagement.MediaType mediaType
    ) {
        engagementRepository.onMediaEngagement();
        repository.startQueueingForMediaEngagement(queueId, contextUrl, mediaType);
    }
}
