package com.glia.widgets.core.visitor;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.VisitorMediaState;

import java.util.HashSet;
import java.util.Set;

public class GliaVisitorMediaRepository implements VisitorMediaUpdatesListener {
    private final Set<VisitorMediaUpdatesListener> visitorMediaUpdatesListeners = new HashSet<>();

    private VisitorMediaState currentMediaState = null;
    private boolean isOnHold = false;

    @Override
    public void onNewVisitorMediaState(VisitorMediaState state) {
        updateVisitorMediaState(state);
        setAudioOnHoldListener();
        setVideoOnHoldListener();
    }

    @Override
    public void onHoldChanged(boolean newOnHold) {
        notifyOnHoldStateChange(newOnHold);
    }

    public void onEngagementStarted(Engagement engagement) {
        engagement.getMedia().on(Media.Events.VISITOR_STATE_UPDATE, this::onNewVisitorMediaState);
    }

    public void onEngagementEnded(Engagement engagement) {
        engagement.getMedia().off(Media.Events.VISITOR_STATE_UPDATE, this::onNewVisitorMediaState);
        currentMediaState = null;
        isOnHold = false;
    }

    public void addVisitorMediaStateListener(VisitorMediaUpdatesListener listener) {
        visitorMediaUpdatesListeners.add(listener);
        updateVisitorMediaState(currentMediaState);
        notifyOnHoldStateChange(isOnHold);
    }

    public void removeVisitorMediaStateListener(VisitorMediaUpdatesListener listener) {
        visitorMediaUpdatesListeners.remove(listener);
    }

    private void updateVisitorMediaState(VisitorMediaState state) {
        currentMediaState = state;
        visitorMediaUpdatesListeners.forEach(listener -> listener.onNewVisitorMediaState(currentMediaState));
    }

    private void notifyOnHoldStateChange(boolean newOnHold) {
        isOnHold = newOnHold;
        visitorMediaUpdatesListeners.forEach(listener -> listener.onHoldChanged(isOnHold));
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
