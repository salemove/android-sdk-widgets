package com.glia.widgets.glia;

import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.model.GliaRepository;

import java.util.function.Consumer;

public class GliaOnEngagementUseCase implements Consumer<OmnicoreEngagement> {

    public interface Listener {
        void newEngagementLoaded(OmnicoreEngagement engagement);
    }

    private final GliaRepository gliaRepository;
    private Listener listener;

    public GliaOnEngagementUseCase(GliaRepository gliaRepository) {
        this.gliaRepository = gliaRepository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        gliaRepository.listenForEngagement(this);
    }

    @Override
    public void accept(OmnicoreEngagement engagement) {
        if (this.listener != null) {
            listener.newEngagementLoaded(engagement);
        }
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            gliaRepository.unregisterEngagementListener(this);
            this.listener = null;
        }
    }
}
