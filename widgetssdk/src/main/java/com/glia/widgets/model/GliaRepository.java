package com.glia.widgets.model;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.VisitorContext;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaRepository {

    private static final String TAG = "GliaTicketRepository";

    public void listenForEngagement(Consumer<OmnicoreEngagement> engagementConsumer) {
        Glia.on(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void unregisterEngagementListener(Consumer<OmnicoreEngagement> engagementConsumer) {
        Glia.off(Glia.Events.ENGAGEMENT, engagementConsumer);
    }

    public void startQueueingForEngagement(
            String queueId,
            String contextUrl,
            Consumer<GliaException> consumer
    ) {
        VisitorContext visitorContext = new VisitorContext(VisitorContext.Type.PAGE, contextUrl);
        Glia.queueForEngagement(queueId, visitorContext, consumer);
    }

    public void listenForQueueTicketChanges(Consumer<QueueTicket> consumer) {
        Glia.on(Glia.Events.QUEUE_TICKET, consumer);
    }

    public void unRegister(Consumer<QueueTicket> consumer) {
        Glia.off(Glia.Events.QUEUE_TICKET, consumer);
    }

    public void cancelTicket(String ticketId) {
        Glia.cancelQueueTicket(ticketId, e -> {
            if (e != null) {
                Logger.e(TAG, "cancelQueueTicketError: " + e.toString());
            }
        });
    }
}
