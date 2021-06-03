package com.glia.widgets.model;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;

public class GliaMediaRepository {
    public interface OperatorMediaStateListener {
        void onNewState(OperatorMediaState state);
    }

    public interface VisitorMediaStateListener {
        void onNewState(VisitorMediaState visitorMediaState);
    }

    public void listenForNewOperatorMediaStates(OperatorMediaStateListener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getMedia().on(Media.Events.OPERATOR_STATE_UPDATE, listener::onNewState);
        });
    }

    public void unregisterListener(OperatorMediaStateListener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getMedia().off(Media.Events.OPERATOR_STATE_UPDATE, listener::onNewState);
        });
    }

    public void listenForNewVisitorMediaStates(VisitorMediaStateListener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getMedia().on(Media.Events.VISITOR_STATE_UPDATE, listener::onNewState);
        });
    }

    public void unregisterListener(VisitorMediaStateListener listener) {
        Glia.getCurrentEngagement().ifPresent(engagement -> {
            engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, listener::onNewState);
        });
    }
}
