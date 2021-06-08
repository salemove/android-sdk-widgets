package com.glia.widgets.glia;

import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.widgets.model.GliaMediaRepository;

public class GliaOnVisitorMediaStateUseCase implements GliaMediaRepository.VisitorMediaStateListener {
    public interface Listener {
        void onNewVisitorMediaState(VisitorMediaState visitorMediaState);
    }

    private final GliaMediaRepository repository;
    private Listener listener;

    public GliaOnVisitorMediaStateUseCase(GliaMediaRepository repository) {
        this.repository = repository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        repository.listenForNewVisitorMediaStates(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            this.listener = null;
            repository.unregisterListener(this);
        }
    }

    @Override
    public void onNewState(VisitorMediaState visitorMediaState) {
        if (this.listener != null) {
            listener.onNewVisitorMediaState(visitorMediaState);
        }
    }
}
