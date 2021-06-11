package com.glia.widgets.glia;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.GliaException;
import com.glia.widgets.model.GliaRepository;

import java.util.function.Consumer;

public class GliaQueueForMediaEngagementUseCase implements Consumer<GliaException> {

    public interface Listener {
        void queueForEngagementSuccess();

        void error(GliaException exception);
    }

    private final GliaRepository repository;
    private Listener listener;

    public GliaQueueForMediaEngagementUseCase(GliaRepository repository) {
        this.repository = repository;
    }

    public void execute(String queueId,
                        String contextUrl,
                        Engagement.MediaType mediaType,
                        Listener listener) {
        this.listener = listener;
        repository.startQueueingForMediaEngagement(queueId, contextUrl, mediaType, this);
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
