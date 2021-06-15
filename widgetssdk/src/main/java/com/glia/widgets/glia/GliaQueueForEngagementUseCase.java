package com.glia.widgets.glia;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.GliaException;
import com.glia.widgets.model.GliaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GliaQueueForEngagementUseCase implements
        Consumer<GliaException>,
        GliaOnQueueTicketUseCase.Listener {

    public interface Listener {
        void queueForEngagementSuccess();

        void error(GliaException exception);
    }

    private final GliaRepository repository;
    private final GliaOnQueueTicketUseCase onQueueTicketUseCase;
    private final List<Listener> listeners = new ArrayList<>();
    private TypeOfOngoingQueueing typeOfOngoingQueueing = TypeOfOngoingQueueing.NONE;

    public GliaQueueForEngagementUseCase(
            GliaRepository repository,
            GliaOnQueueTicketUseCase onQueueTicketUseCase
    ) {
        this.repository = repository;
        this.onQueueTicketUseCase = onQueueTicketUseCase;
    }

    public TypeOfOngoingQueueing execute(String queueId, String contextUrl, Listener listener) {
        this.listeners.add(listener);
        if (typeOfOngoingQueueing == TypeOfOngoingQueueing.NONE) {
            typeOfOngoingQueueing = TypeOfOngoingQueueing.CHAT;
            repository.startQueueingForEngagement(queueId, contextUrl, this);
        }
        return typeOfOngoingQueueing;
    }

    public TypeOfOngoingQueueing execute(
            String queueId,
            String contextUrl,
            Engagement.MediaType mediaType,
            Listener listener
    ) {
        this.listeners.add(listener);
        if (typeOfOngoingQueueing == TypeOfOngoingQueueing.NONE) {
            typeOfOngoingQueueing = TypeOfOngoingQueueing.MEDIA;
            repository.startQueueingForMediaEngagement(queueId, contextUrl, mediaType, this);
        }
        return typeOfOngoingQueueing;
    }

    public void unregisterListener(Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void accept(GliaException exception) {
        if (!this.listeners.isEmpty()) {
            if (exception != null) {
                for (Listener listener : listeners) {
                    listener.error(exception);
                }
            } else {
                for (Listener listener : listeners) {
                    listener.queueForEngagementSuccess();
                }
            }
        }
    }

    @Override
    public void ticketLoaded(String ticket) {
        typeOfOngoingQueueing = TypeOfOngoingQueueing.NONE;
    }

    public TypeOfOngoingQueueing getTypeOfOngoingQueueing(){
        return typeOfOngoingQueueing;
    }

    public enum TypeOfOngoingQueueing {
        NONE, CHAT, MEDIA
    }
}
