package com.glia.widgets.core.queue.domain;

import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;

public class UnsubscribeFromQueueingStateChangeUseCase {
    private final GliaQueueRepository gliaQueueRepository;

    public UnsubscribeFromQueueingStateChangeUseCase(
            GliaQueueRepository queueRepository
    ) {
        gliaQueueRepository = queueRepository;
    }

    public void execute(QueueTicketsEventsListener listener) {
        gliaQueueRepository.removeEventListener(listener);
    }
}
