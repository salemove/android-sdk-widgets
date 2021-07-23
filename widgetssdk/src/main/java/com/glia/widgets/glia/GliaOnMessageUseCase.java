package com.glia.widgets.glia;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.model.GliaChatRepository;

public class GliaOnMessageUseCase implements
        GliaOnEngagementUseCase.Listener,
        GliaChatRepository.MessageListener {

    public interface Listener {
        void onMessage(ChatMessage message);
    }

    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final GliaChatRepository messageRepository;
    private Listener listener;

    public GliaOnMessageUseCase(
            GliaChatRepository messageRepository,
            GliaOnEngagementUseCase gliaOnEngagementUseCase
    ) {
        this.onEngagementUseCase = gliaOnEngagementUseCase;
        this.messageRepository = messageRepository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        this.onEngagementUseCase.execute(this);
    }

    public void unregisterListener() {
        if (this.listener != null) {
            messageRepository.unregisterMessageListener(this);
            onEngagementUseCase.unregisterListener(this);
            this.listener = null;
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        messageRepository.listenForMessages(this, engagement);
    }

    @Override
    public void onMessage(ChatMessage chatMessage) {
        if (this.listener != null) {
            listener.onMessage(chatMessage);
        }
    }
}
