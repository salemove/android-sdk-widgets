package com.glia.widgets.glia;

import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.model.GliaMediaRepository;

public class GliaOnVisitorMediaStateUseCase implements
        GliaMediaRepository.VisitorMediaStateListener,
        GliaOnEngagementUseCase.Listener {
    private final static String TAG = "GliaOnVisitorMediaStateUseCase";

    public interface Listener {
        void onNewVisitorMediaState(VisitorMediaState visitorMediaState);
    }

    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final GliaMediaRepository repository;
    private Listener listener;

    public GliaOnVisitorMediaStateUseCase(GliaOnEngagementUseCase onEngagementUseCase,
                                          GliaMediaRepository repository) {
        this.onEngagementUseCase = onEngagementUseCase;
        this.repository = repository;
    }

    public void execute(Listener listener) {
        Logger.d(TAG, "execute");
        this.listener = listener;
        onEngagementUseCase.execute(this);
    }

    public void unregisterListener(Listener listener) {
        Logger.d(TAG, "unregisterListener");
        if (this.listener == listener) {
            this.listener = null;
            repository.unregisterListener(this);
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "newEngagementLoaded");
        repository.listenForNewVisitorMediaStates(this);
    }

    @Override
    public void onNewState(VisitorMediaState visitorMediaState) {
        Logger.d(TAG, "onNewState- hasVideo:" +
                Boolean.valueOf(visitorMediaState.getVideo() != null).toString() +
                ", hasAudio: " +
                Boolean.valueOf(visitorMediaState.getAudio() != null).toString());
        if (this.listener != null) {
            listener.onNewVisitorMediaState(visitorMediaState);
        }
    }
}
