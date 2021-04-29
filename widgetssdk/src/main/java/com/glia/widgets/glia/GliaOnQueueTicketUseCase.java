package com.glia.widgets.glia;

import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.model.GliaTicketRepository;

public class GliaOnQueueTicketUseCase implements GliaTicketRepository.TicketChangesListener {

    public interface Listener {
        void ticketLoaded(String ticket);
    }

    private final GliaTicketRepository repository;
    private Listener listener;

    public GliaOnQueueTicketUseCase(GliaTicketRepository repository) {
        this.repository = repository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        repository.listenForQueueTicketChanges(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public void newTicket(QueueTicket ticket) {
        if (this.listener != null) {
            listener.ticketLoaded(ticket.getId());
        }
    }
}
