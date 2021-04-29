package com.glia.widgets.glia;

import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.model.GliaEngagementRepository;

public class GliaOnEngagementUseCase implements GliaEngagementRepository.EngagementListener {

    public interface Listener {
        void newEngagementLoaded(OmnicoreEngagement engagement);
    }

    private final GliaEngagementRepository gliaEngagementRepository;
    private Listener listener;

    public GliaOnEngagementUseCase(GliaEngagementRepository gliaEngagementRepository) {
        this.gliaEngagementRepository = gliaEngagementRepository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        gliaEngagementRepository.listenForEngagement(this);
    }

    @Override
    public void success(OmnicoreEngagement engagement) {
        if (this.listener != null) {
            listener.newEngagementLoaded(engagement);
        }
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            gliaEngagementRepository.unregisterEngagementListener(this);
            this.listener = null;
        }
    }
}
