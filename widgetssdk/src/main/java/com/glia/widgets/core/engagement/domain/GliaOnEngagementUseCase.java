package com.glia.widgets.core.engagement.domain;

import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.GliaEngagementStateRepository;
import com.glia.widgets.core.engagement.GliaOperatorRepository;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.queue.GliaQueueRepository;
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository;

import java.util.function.Consumer;

public class GliaOnEngagementUseCase implements Consumer<OmnicoreEngagement> {

    private final GliaEngagementRepository gliaRepository;
    private final GliaOperatorMediaRepository operatorMediaRepository;
    private final GliaQueueRepository gliaQueueRepository;
    private final GliaVisitorMediaRepository gliaVisitorMediaRepository;
    private final GliaEngagementStateRepository gliaEngagementStateRepository;
    private final GliaOperatorRepository operatorRepository;
    private Listener listener;

    public GliaOnEngagementUseCase(
        GliaEngagementRepository gliaRepository,
        GliaOperatorMediaRepository operatorMediaRepository,
        GliaQueueRepository gliaQueueRepository,
        GliaVisitorMediaRepository gliaVisitorMediaRepository,
        GliaEngagementStateRepository gliaEngagementStateRepository,
        GliaOperatorRepository operatorRepository
    ) {
        this.gliaRepository = gliaRepository;
        this.operatorMediaRepository = operatorMediaRepository;
        this.gliaQueueRepository = gliaQueueRepository;
        this.gliaVisitorMediaRepository = gliaVisitorMediaRepository;
        this.gliaEngagementStateRepository = gliaEngagementStateRepository;
        this.operatorRepository = operatorRepository;
    }

    public void execute(Listener listener) {
        if (this.listener == listener) {
            // Already listening
            return;
        }
        this.listener = listener;
        gliaRepository.listenForOmnicoreEngagement(this);
    }

    @Override
    public void accept(OmnicoreEngagement engagement) {
        operatorRepository.emit(engagement.getState().getOperator());
        operatorMediaRepository.onEngagementStarted(engagement);
        gliaVisitorMediaRepository.onEngagementStarted(engagement);
        gliaEngagementStateRepository.onEngagementStarted(engagement);
        gliaQueueRepository.onEngagementStarted();
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

    public interface Listener {
        void newEngagementLoaded(OmnicoreEngagement engagement);
    }
}