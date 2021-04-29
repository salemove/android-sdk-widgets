package com.glia.widgets.glia;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.model.GliaTicketRepository;

public class GliaQueueForEngagementUseCase implements GliaTicketRepository.QueueForEngagementListener {

    public interface Listener {
        void queueForEngagementSuccess();
        void error(GliaException exception);
    }

    private final GliaTicketRepository repository;
    private Listener listener;

    public GliaQueueForEngagementUseCase(GliaTicketRepository repository) {
        this.repository = repository;
    }

    public void execute(String queueId, String contextUrl, Listener listener) {
        this.listener = listener;
        repository.startQueueingForEngagement(queueId, contextUrl, this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public void success(GliaException exception) {
        if (exception != null) {
            this.listener.error(exception);
        }
    }
}
