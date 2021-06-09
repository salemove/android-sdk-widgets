package com.glia.widgets.glia;

import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.model.GliaMediaRepository;

public class GliaOnOperatorMediaStateUseCase implements
        GliaMediaRepository.OperatorMediaStateListener,
        GliaOnEngagementUseCase.Listener {

    private final static String TAG = "GliaOnOperatorMediaStateUseCase";

    public interface Listener {
        void onNewOperatorMediaState(OperatorMediaState operatorMediaState);
    }

    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final GliaMediaRepository repository;
    private Listener listener;

    public GliaOnOperatorMediaStateUseCase(GliaOnEngagementUseCase gliaOnEngagementUseCase,
                                           GliaMediaRepository repository) {
        this.onEngagementUseCase = gliaOnEngagementUseCase;
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
            onEngagementUseCase.unregisterListener(this);
            repository.unregisterListener(this);
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "newEngagementLoaded");
        repository.listenForNewOperatorMediaStates(this);
    }

    @Override
    public void onNewState(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "onNewState- hasVideo:" +
                Boolean.valueOf(operatorMediaState.getVideo() != null).toString() +
                ", hasAudio: " +
                Boolean.valueOf(operatorMediaState.getAudio() != null).toString());
        if (this.listener != null) {
            listener.onNewOperatorMediaState(operatorMediaState);
        }
    }
}
