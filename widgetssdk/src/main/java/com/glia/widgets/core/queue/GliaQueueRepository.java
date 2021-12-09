package com.glia.widgets.core.queue;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.VisitorContext;
import com.glia.androidsdk.queuing.QueueTicket;
import com.glia.widgets.helper.Logger;

import java.util.ArrayList;
import java.util.function.Consumer;

public class GliaQueueRepository {
    private static final String TAG = GliaQueueRepository.class.getSimpleName();

    private static final int MEDIA_PERMISSION_REQUEST_CODE = 1001;

    public enum TypeOfOngoingQueueing {
        NONE, CHAT, MEDIA
    }

    private final ArrayList<QueueTicketsEventsListener> eventsListeners = new ArrayList<>();
    private TypeOfOngoingQueueing typeOfOngoingQueueing = TypeOfOngoingQueueing.NONE;
    private String queueTicket = null;

    private final Consumer<QueueTicket> ticketConsumer = queueTicket -> {
        if (queueTicket != null) {
            this.queueTicket = queueTicket.getId();
        }
    };

    private final Consumer<GliaException> startQueueingExceptionConsumer = exception -> {
        if (exception != null) {
            if (exception.cause == GliaException.Cause.ALREADY_QUEUED) {

                for (QueueTicketsEventsListener listener : eventsListeners) {
                    listener.started();
                }
            } else {
                for (QueueTicketsEventsListener listener : eventsListeners) {
                    listener.error(exception);
                }
            }
        } else {
            for (QueueTicketsEventsListener listener : eventsListeners) {
                listener.started();
            }
        }
    };

    private final Consumer<GliaException> stopQueueingExceptionConsumer = exception -> {
        if (exception != null) {
            Logger.e(TAG, "cancelQueueTicketError: " + exception.toString());
        } else {
            Logger.d(TAG, "cancelQueueTicketSuccess");
            for (QueueTicketsEventsListener listener : eventsListeners) {
                listener.stopped();
            }
        }
    };

    public void cleanOnEngagementEnd() {
        Glia.off(Glia.Events.QUEUE_TICKET, ticketConsumer);
        cancelTicket();
        typeOfOngoingQueueing = TypeOfOngoingQueueing.NONE;
        eventsListeners.clear();
    }

    public void cancelTicket() {
        if (queueTicket != null) Glia.cancelQueueTicket(queueTicket, stopQueueingExceptionConsumer);
        typeOfOngoingQueueing = TypeOfOngoingQueueing.NONE;
    }

    public boolean isNoQueueingOngoing() {
        return typeOfOngoingQueueing == TypeOfOngoingQueueing.NONE;
    }

    public boolean isMediaQueueingOngoing() {
        return typeOfOngoingQueueing == TypeOfOngoingQueueing.MEDIA;
    }

    public void addOngoingQueueingEventListener(QueueTicketsEventsListener listener) {
        eventsListeners.add(listener);
        listener.ongoing();
    }

    public void startQueueingForEngagement(
            String queueId,
            String contextUrl,
            QueueTicketsEventsListener listener
    ) {
        eventsListeners.add(listener);
        typeOfOngoingQueueing = TypeOfOngoingQueueing.CHAT;
        VisitorContext visitorContext = new VisitorContext(VisitorContext.Type.PAGE, contextUrl);
        Glia.on(Glia.Events.QUEUE_TICKET, ticketConsumer);
        Glia.queueForEngagement(queueId, visitorContext, startQueueingExceptionConsumer);
    }

    public void startQueueingForMediaEngagement(String queueId,
                                                String contextUrl,
                                                Engagement.MediaType mediaType,
                                                QueueTicketsEventsListener listener
    ) {
        eventsListeners.add(listener);
        typeOfOngoingQueueing = TypeOfOngoingQueueing.MEDIA;
        VisitorContext visitorContext = new VisitorContext(VisitorContext.Type.PAGE, contextUrl);
        Glia.on(Glia.Events.QUEUE_TICKET, ticketConsumer);
        Glia.queueForEngagement(queueId, mediaType, visitorContext, MEDIA_PERMISSION_REQUEST_CODE, startQueueingExceptionConsumer);
    }

    public String getQueueTicket() {
        return queueTicket;
    }

    public void onEngagementStarted() {
        typeOfOngoingQueueing = TypeOfOngoingQueueing.NONE;
    }
}
