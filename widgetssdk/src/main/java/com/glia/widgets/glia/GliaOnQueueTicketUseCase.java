package com.glia.widgets.glia;

import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.model.GliaRepository;

import java.util.function.Consumer;

public class GliaOnQueueTicketUseCase implements Consumer<QueueTicket> {

    public interface Listener {
        void ticketLoaded(String ticket);
    }

    private final GliaRepository repository;
    private Listener listener;

    public GliaOnQueueTicketUseCase(GliaRepository repository) {
        this.repository = repository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        repository.listenForQueueTicketChanges(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            repository.unRegister(this);
            this.listener = null;
        }
    }

    @Override
    public void accept(QueueTicket ticket) {
        if (this.listener != null) {
            listener.ticketLoaded(ticket.getId());
        }
    }
}
