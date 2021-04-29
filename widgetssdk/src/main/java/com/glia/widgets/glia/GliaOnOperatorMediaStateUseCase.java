package com.glia.widgets.glia;

import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.widgets.model.GliaMediaStateRepository;

public class GliaOnOperatorMediaStateUseCase implements GliaMediaStateRepository.OperatorMediaStateListener {

    public interface Listener {
        void onNewOperatorMediaState(OperatorMediaState operatorMediaState);
    }

    private final GliaMediaStateRepository repository;
    private Listener listener;

    public GliaOnOperatorMediaStateUseCase(GliaMediaStateRepository repository) {
        this.repository = repository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        repository.listenForNewOperatorMediaStates(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            this.listener = null;
            repository.unregisterListener(this);
        }
    }

    @Override
    public void onNewState(OperatorMediaState operatorMediaState) {
        if (this.listener != null) {
            listener.onNewOperatorMediaState(operatorMediaState);
        }
    }
}
