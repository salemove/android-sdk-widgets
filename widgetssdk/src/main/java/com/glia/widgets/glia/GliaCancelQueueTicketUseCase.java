package com.glia.widgets.glia;

import com.glia.widgets.model.GliaTicketRepository;

public class GliaCancelQueueTicketUseCase {

    private final GliaTicketRepository repository;

    public GliaCancelQueueTicketUseCase(GliaTicketRepository repository) {
        this.repository = repository;
    }

    public void execute(String ticketId) {
        repository.cancelTicket(ticketId);
    }
}
