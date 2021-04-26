package com.glia.widgets.glia;

import com.glia.widgets.helper.BaseObservable;
import com.glia.widgets.model.GliaTicketRepository;

public class GliaQueueForEngagementUseCase extends BaseObservable<GliaQueueForEngagementUseCase.Listener>
        implements GliaTicketRepository.Listener {

    public interface Listener {
        void ticketLoaded(String ticket);

        void error(Throwable error);
    }

    private final GliaTicketRepository repository;

    public GliaQueueForEngagementUseCase(GliaTicketRepository repository) {
        this.repository = repository;
    }

    public void execute(String queueId, String contextUrl) {
        repository.execute(queueId, contextUrl);
    }

    @Override
    public void queueForTicketSuccess(String ticketId) {
        notifySuccess(ticketId);
    }

    @Override
    public void error(Throwable throwable) {
        notifyFailure(throwable);
    }

    private void notifySuccess(String ticket) {
        for (Listener listener : getListeners()) {
            listener.ticketLoaded(ticket);
        }
    }

    private void notifyFailure(Throwable error) {
        for (Listener listener : getListeners()) {
            listener.error(error);
        }
    }

    @Override
    protected void onFirstListenerRegistered() {
        repository.registerListener(this);
    }

    @Override
    protected void onLastListenerUnregistered() {
        repository.unregisterListener(this);
    }
}
