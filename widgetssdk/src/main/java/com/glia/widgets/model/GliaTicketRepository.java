package com.glia.widgets.model;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.VisitorContext;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.helper.Logger;

public class GliaTicketRepository {

    private static final String TAG = "GliaTicketRepository";

    public interface TicketChangesListener {
        void newTicket(QueueTicket ticket);
    }

    public interface QueueForEngagementListener {
        void success(GliaException exception);
    }

    public void startQueueingForEngagement(
            String queueId,
            String contextUrl,
            QueueForEngagementListener listener
    ) {
        VisitorContext visitorContext = new VisitorContext(VisitorContext.Type.PAGE, contextUrl);
        Glia.queueForEngagement(queueId, visitorContext, listener::success);
    }

    public void listenForQueueTicketChanges(TicketChangesListener ticketChangesListener) {
        Glia.on(Glia.Events.QUEUE_TICKET, ticketChangesListener::newTicket);
    }

    public void unRegister(TicketChangesListener ticketChangesListener) {
        Glia.off(Glia.Events.QUEUE_TICKET, ticketChangesListener::newTicket);
    }

    public void cancelTicket(String ticketId) {
        Glia.cancelQueueTicket(ticketId, e -> {
            if (e != null) {
                Logger.e(TAG, "cancelQueueTicketError: " + e.toString());
            }
        });
    }
}
