package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;

import io.reactivex.Completable;

public class QueueTicketStateChangeToUnstaffedUseCase {
    private final GliaQueueRepository gliaQueueRepository;

    public QueueTicketStateChangeToUnstaffedUseCase(GliaQueueRepository gliaQueueRepository) {
        this.gliaQueueRepository = gliaQueueRepository;
    }

    public Completable execute() {
        return gliaQueueRepository.observeQueueTicketStateChangeToUnstaffed();
    }
}
