package com.glia.widgets.core.operator;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.OperatorMediaState;

import java.util.ArrayList;
import java.util.function.Consumer;

public class GliaOperatorMediaRepository {
    public interface OperatorMediaStateListener {
        void onNewState(OperatorMediaState state);
    }

    private final ArrayList<OperatorMediaStateListener> eventListeners = new ArrayList<>();

    private final Consumer<OperatorMediaState> operatorMediaStateConsumer = operatorMediaState -> {
        this.operatorMediaState = operatorMediaState;
        for (OperatorMediaStateListener listener : eventListeners) {
            listener.onNewState(operatorMediaState);
        }
    };

    private OperatorMediaState operatorMediaState = null;

    public void addMediaStateListener(OperatorMediaStateListener listener) {
        eventListeners.add(listener);
        if (operatorMediaState != null) listener.onNewState(operatorMediaState);
    }

    public void startListening(Engagement engagement) {
        engagement.getMedia()
                .on(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
    }

    public void stopListening(Engagement engagement) {
        operatorMediaState = null;
        eventListeners.clear();
        engagement.getMedia().off(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateConsumer);
    }

    public boolean isOperatorMediaState() {
        return operatorMediaState != null &&
                (operatorMediaState.getAudio() != null || operatorMediaState.getVideo() != null);
    }
}
