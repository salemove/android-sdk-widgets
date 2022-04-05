package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;

public class SubscribeToQueueingStateChangeUseCase {
    private final GliaQueueRepository gliaQueueRepository;

    public SubscribeToQueueingStateChangeUseCase(
            GliaQueueRepository queueRepository
    ) {
        gliaQueueRepository = queueRepository;
    }

    public void execute(QueueTicketsEventsListener listener) {
        gliaQueueRepository.addEventListener(listener);
    }
}
