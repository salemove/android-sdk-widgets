package com.glia.widgets.chat.domain;

import com.glia.androidsdk.chat.OperatorTypingStatus;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.chat.data.GliaChatRepository;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;

public class GliaOnOperatorTypingUseCase implements
        GliaOnEngagementUseCase.Listener,
        GliaChatRepository.OperatorTypingListener {

    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final GliaChatRepository messageRepository;
    private GliaOnOperatorTypingUseCase.Listener listener;

    public GliaOnOperatorTypingUseCase(
            GliaChatRepository messageRepository,
            GliaOnEngagementUseCase gliaOnEngagementUseCase
    ) {
        this.onEngagementUseCase = gliaOnEngagementUseCase;
        this.messageRepository = messageRepository;
    }

    public void execute(GliaOnOperatorTypingUseCase.Listener listener) {
        this.listener = listener;
        this.onEngagementUseCase.execute(this);
    }

    public void unregisterListener() {
        if (this.listener != null) {
            messageRepository.unregisterOperatorTypingListener(this);
            onEngagementUseCase.unregisterListener(this);
            this.listener = null;
        }
    }

    @Override
    public void onOperatorTyping(OperatorTypingStatus operatorTypingStatus) {
        if (this.listener != null) {
            listener.onOperatorTyping(operatorTypingStatus.isTyping());
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        messageRepository.listenForOperatorTyping(this, engagement);
    }

    public interface Listener {
        void onOperatorTyping(boolean isOperatorTyping);
    }
}
