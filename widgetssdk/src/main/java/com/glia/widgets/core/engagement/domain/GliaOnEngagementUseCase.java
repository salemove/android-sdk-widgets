package com.glia.widgets.core.engagement.domain;

import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;

import java.util.function.Consumer;

public class GliaOnEngagementUseCase implements Consumer<OmnicoreEngagement> {

    public interface Listener {
        void newEngagementLoaded(OmnicoreEngagement engagement);
    }

    private final GliaEngagementRepository gliaRepository;
    private final GliaOperatorMediaRepository operatorMediaRepository;
    private final GliaQueueRepository gliaQueueRepository;
    private final GliaVisitorMediaRepository gliaVisitorMediaRepository;
    private Listener listener;

    public GliaOnEngagementUseCase(
            GliaEngagementRepository gliaRepository,
            GliaOperatorMediaRepository operatorMediaRepository,
            GliaQueueRepository gliaQueueRepository,
            GliaVisitorMediaRepository gliaVisitorMediaRepository
    ) {
        this.gliaRepository = gliaRepository;
        this.operatorMediaRepository = operatorMediaRepository;
        this.gliaQueueRepository = gliaQueueRepository;
        this.gliaVisitorMediaRepository = gliaVisitorMediaRepository;
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
        operatorMediaRepository.startListening(engagement);
        gliaVisitorMediaRepository.onEngagementStarted(engagement);
        gliaQueueRepository.onEngagementStarted();
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            gliaRepository.unregisterEngagementListener(this);
            this.listener = null;
        }
    }
}
