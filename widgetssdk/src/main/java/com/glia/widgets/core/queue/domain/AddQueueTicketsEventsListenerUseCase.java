package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;

public class AddQueueTicketsEventsListenerUseCase {
    private final GliaQueueRepository repository;

    public AddQueueTicketsEventsListenerUseCase(GliaQueueRepository repository) {
        this.repository = repository;
    }

    public void execute(QueueTicketsEventsListener listener) {
        this.repository.addOngoingQueueingEventListener(listener);
    }
}
