package com.glia.widgets.core.visitor;

import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.di.Dependencies;

public class GliaVisitorMediaRepository {
    public interface VisitorMediaStateListener {
        void onNewState(VisitorMediaState visitorMediaState);
    }

    public void listenForNewVisitorMediaStates(VisitorMediaStateListener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getMedia().on(Media.Events.VISITOR_STATE_UPDATE, listener::onNewState));
    }

    public void unregisterListener(VisitorMediaStateListener listener) {
        Dependencies.glia().getCurrentEngagement().ifPresent(engagement ->
                engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, listener::onNewState));
    }
}
