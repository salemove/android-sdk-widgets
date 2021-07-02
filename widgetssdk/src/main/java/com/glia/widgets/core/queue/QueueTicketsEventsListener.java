package com.glia.widgets.core.queue;

import com.glia.androidsdk.GliaException;

public interface QueueTicketsEventsListener {
    void onTicketReceived(String ticketId);

    void started();

    void ongoing();

    void stopped();

    void error(GliaException exception);
}
