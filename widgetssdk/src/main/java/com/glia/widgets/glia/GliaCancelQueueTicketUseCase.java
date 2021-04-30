package com.glia.widgets.glia;

import com.glia.widgets.model.GliaRepository;

public class GliaCancelQueueTicketUseCase {

    private final GliaRepository repository;

    public GliaCancelQueueTicketUseCase(GliaRepository repository) {
        this.repository = repository;
    }

    public void execute(String ticketId) {
        repository.cancelTicket(ticketId);
    }
}
