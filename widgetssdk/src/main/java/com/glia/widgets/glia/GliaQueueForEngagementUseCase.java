package com.glia.widgets.glia;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.model.GliaRepository;

import java.util.function.Consumer;

public class GliaQueueForEngagementUseCase implements Consumer<GliaException> {

    public interface Listener {
        void queueForEngagementSuccess();

        void error(GliaException exception);
    }

    private final GliaRepository repository;
    private Listener listener;

    public GliaQueueForEngagementUseCase(GliaRepository repository) {
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
    public void accept(GliaException exception) {
        if (listener != null) {
            if (exception != null) {
                this.listener.error(exception);
            } else {
                this.listener.queueForEngagementSuccess();
            }
        }
    }
}
