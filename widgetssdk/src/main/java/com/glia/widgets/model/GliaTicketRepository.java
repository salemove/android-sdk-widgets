package com.glia.widgets.model;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.VisitorContext;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.helper.BaseObservable;
import com.glia.widgets.helper.Logger;

import java.util.function.Consumer;

public class GliaTicketRepository extends BaseObservable<GliaTicketRepository.Listener> {

    private static String TAG = "GliaTicketRepository";

    public interface Listener {

        void queueForTicketSuccess(String ticketId);

        void error(Throwable throwable);
    }

    private Consumer<GliaException> queueForEngagementConsumer;
    private Consumer<QueueTicket> ticketConsumer;

    public void execute(String queueId, String contextUrl) {
        VisitorContext visitorContext = new VisitorContext(VisitorContext.Type.PAGE, contextUrl);
        Glia.queueForEngagement(queueId, visitorContext, queueForEngagementConsumer);
        Glia.on(Glia.Events.QUEUE_TICKET, ticketConsumer);
    }

    private void notifySuccess(String ticket) {
        for (Listener listener : getListeners()) {
            listener.queueForTicketSuccess(ticket);
        }
    }

    private void notifyFailure(Throwable error) {
        for (Listener listener : getListeners()) {
            listener.error(error);
        }
    }

    @Override
    protected void onFirstListenerRegistered() {
        queueForEngagementConsumer = response -> {
            if (response != null) {
                notifyFailure(response);
            } else {
                Logger.d(TAG, "queueForEngagementSuccess");
            }
        };
        ticketConsumer = ticket -> notifySuccess(ticket.getId());
    }

    @Override
    protected void onLastListenerUnregistered() {
        Glia.off(Glia.Events.QUEUE_TICKET, ticketConsumer);
        queueForEngagementConsumer = null;
        ticketConsumer = null;
    }
}
