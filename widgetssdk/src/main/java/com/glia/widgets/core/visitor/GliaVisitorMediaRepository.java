package com.glia.widgets.core.visitor;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.VisitorMediaState;

import java.util.ArrayList;
import java.util.List;

public class GliaVisitorMediaRepository implements VisitorMediaStateListener {
    private final List<VisitorMediaStateListener> visitorMediaStateUpdates = new ArrayList<>();

    private VisitorMediaState currentMediaState = null;
    private boolean isOnHold = false;

    @Override
    public void onNewVisitorMediaState(VisitorMediaState visitorMediaState) {
        currentMediaState = visitorMediaState;
        visitorMediaStateUpdates.forEach(listener -> listener.onNewVisitorMediaState(currentMediaState));
        setAudioOnHoldListener();
        setVideoOnHoldListener();
    }

    @Override
    public void onHoldChanged(boolean newOnHold) {
        if (isOnHold != newOnHold) {
            isOnHold = newOnHold;
            visitorMediaStateUpdates.forEach(listener -> listener.onHoldChanged(isOnHold));
        }
    }

    public void onEngagementStarted(Engagement engagement) {
        engagement.getMedia().on(Media.Events.VISITOR_STATE_UPDATE, this::onNewVisitorMediaState);
    }

    public void onEngagementEnded(Engagement engagement) {
        engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, this::onNewVisitorMediaState);
        currentMediaState = null;
        isOnHold = false;
    }

    public void addVisitorMediaStateListener(VisitorMediaStateListener listener) {
        listener.onNewVisitorMediaState(currentMediaState);
        listener.onHoldChanged(isOnHold);
        visitorMediaStateUpdates.add(listener);
    }

    public void removeVisitorMediaStateListener(VisitorMediaStateListener listener) {
        visitorMediaStateUpdates.remove(listener);
    }

    private boolean hasVisitorVideoMedia() {
        return currentMediaState != null && currentMediaState.getVideo() != null;
    }

    private boolean hasVisitorAudioMedia() {
        return currentMediaState != null && currentMediaState.getAudio() != null;
    }

    private void setVideoOnHoldListener() {
        if (hasVisitorVideoMedia()) {
            currentMediaState.getVideo().setOnHoldHandler(this::onHoldChanged);
        }
    }

    private void setAudioOnHoldListener() {
        if (hasVisitorAudioMedia()) {
            currentMediaState.getAudio().setOnHoldHandler(this::onHoldChanged);
        }
    }
}
