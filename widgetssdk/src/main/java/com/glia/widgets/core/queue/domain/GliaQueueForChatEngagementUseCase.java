package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.domain.exception.EngagementOngoingException;
import com.glia.widgets.core.queue.domain.exception.QueueingOngoingException;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.engagement.HasOngoingEngagementUseCase;
import com.glia.widgets.helper.rx.Schedulers;

import io.reactivex.Completable;

public class GliaQueueForChatEngagementUseCase {
    private final Schedulers schedulers;
    private final HasOngoingEngagementUseCase hasOngoingEngagementUseCase;
    private final GliaQueueRepository repository;

    public GliaQueueForChatEngagementUseCase(
        Schedulers schedulers,
        GliaQueueRepository repository,
        HasOngoingEngagementUseCase hasOngoingEngagementUseCase
    ) {
        this.repository = repository;
        this.hasOngoingEngagementUseCase = hasOngoingEngagementUseCase;
        this.schedulers = schedulers;
    }

    public Completable execute(String queueId, String  visitorContextAssetId) {
        if (hasOngoingEngagementUseCase.invoke()) {
            return Completable.error(new EngagementOngoingException());
        } else {
            return startQueueing(queueId, visitorContextAssetId);
        }
    }

    private Completable startQueueing(String queueId, String visitorContextAssetId) {
        GliaQueueingState queueingState = repository.getQueueingState();
        if (queueingState instanceof GliaQueueingState.None) {
            return repository
                    .startQueueingForEngagement(queueId, visitorContextAssetId)
                    .subscribeOn(schedulers.getComputationScheduler())
                    .observeOn(schedulers.getMainScheduler());

        } else {
            return Completable.error(new QueueingOngoingException());
        }
    }
}
