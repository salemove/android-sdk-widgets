package com.glia.widgets.core.operator;

import androidx.annotation.Nullable;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.OperatorMediaState;

import java.util.ArrayList;
import java.util.function.Consumer;

public class GliaOperatorMediaRepository {
    private final ArrayList<OperatorMediaStateListener> eventListeners = new ArrayList<>();

    private final Consumer<OperatorMediaState> operatorMediaStateConsumer = this::notifyOperatorMediaStateChanged;

    private OperatorMediaState currentMediaState = null;

    public void addMediaStateListener(OperatorMediaStateListener listener) {
        eventListeners.add(listener);
        if (currentMediaState != null) listener.onNewState(currentMediaState);
    }

    public void removeMediaStateListener(OperatorMediaStateListener listener) {
        eventListeners.remove(listener);
    }

    public void onEngagementStarted(Engagement engagement) {
        engagement.getMedia()
                .on(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
    }

    public void onEngagementEnded(Engagement engagement) {
        currentMediaState = null;
        notifyOperatorMediaStateChanged(null);
        engagement.getMedia().off(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
    }

    public boolean hasOperatorMedia() {
        return hasOperatorAudioMedia() || hasOperatorVideoMedia();
    }

    public boolean hasOperatorAudioMedia() {
        return currentMediaState != null && currentMediaState.getAudio() != null;
    }

    public boolean hasOperatorVideoMedia() {
        return currentMediaState != null && currentMediaState.getVideo() != null;
    }

    private void notifyOperatorMediaStateChanged(OperatorMediaState state) {
        this.currentMediaState = state;
        for (OperatorMediaStateListener listener : eventListeners) {
            listener.onNewState(state);
        }
    }

    public interface OperatorMediaStateListener {
        void onNewState(@Nullable OperatorMediaState state);
    }
}
