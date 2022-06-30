package com.glia.widgets.core.queue.domain;

import com.glia.androidsdk.Engagement;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.domain.exception.EngagementOngoingException;
import com.glia.widgets.core.queue.domain.exception.QueueingOngoingException;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.helper.rx.Schedulers;

import io.reactivex.Completable;

public class GliaQueueForMediaEngagementUseCase {
    private final Schedulers schedulers;
    private final GliaQueueRepository repository;
    private final GliaEngagementRepository engagementRepository;

    public GliaQueueForMediaEngagementUseCase(
            Schedulers schedulers,
            GliaQueueRepository repository,
            GliaEngagementRepository engagementRepository
    ) {
        this.repository = repository;
        this.engagementRepository = engagementRepository;
        this.schedulers = schedulers;
    }

    public Completable execute(
            String queueId,
            String contextUrl,
            Engagement.MediaType mediaType
    ) {
        if (engagementRepository.hasOngoingEngagement()) {
            return Completable.error(new EngagementOngoingException());
        } else {
            return startQueueing(queueId, contextUrl, mediaType);
        }
    }

    private Completable startQueueing(
            String queueId,
            String contextUrl,
            Engagement.MediaType mediaType
    ) {
        GliaQueueingState queueingState = repository.getQueueingState();
        if (queueingState instanceof GliaQueueingState.None) {
            return repository.startQueueingForMediaEngagement(queueId, contextUrl, mediaType)
                    .subscribeOn(schedulers.getComputationScheduler())
                    .observeOn(schedulers.getMainScheduler());
        } else {
            return Completable.error(new QueueingOngoingException());
        }
    }
}
