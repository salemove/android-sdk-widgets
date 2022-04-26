package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.domain.exception.IllegalQueueingStateException;
import com.glia.widgets.core.queue.domain.exception.NoQueueingOngoingException;
import com.glia.widgets.core.queue.model.GliaQueueingState;
import com.glia.widgets.helper.rx.Schedulers;

import io.reactivex.Completable;

public class GliaCancelQueueTicketUseCase {
    private final Schedulers schedulers;
    private final GliaQueueRepository repository;

    public GliaCancelQueueTicketUseCase(Schedulers schedulers, GliaQueueRepository repository) {
        this.repository = repository;
        this.schedulers = schedulers;
    }

    public Completable execute() {
        return tryCancelQueueing(repository.getQueueingState())
                .subscribeOn(schedulers.getComputationScheduler())
                .observeOn(schedulers.getMainScheduler());
    }

    private Completable tryCancelQueueing(GliaQueueingState queueingState) {
        if (queueingState instanceof GliaQueueingState.None) {
            return Completable.error(new NoQueueingOngoingException());
        } else {
            if (queueingState instanceof GliaQueueingState.Chat || queueingState instanceof GliaQueueingState.Media) {
                return repository.cancelTicket(queueingState.getTicketId());
            } else {
                return Completable.error(new IllegalQueueingStateException());
            }
        }
    }
}
