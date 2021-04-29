package com.glia.widgets.glia;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.model.GliaMessageRepository;

public class GliaOnMessageUseCase implements
        GliaMessageRepository.MessageListener {

    public interface Listener {
        void onMessage(ChatMessage message);
    }

    private final GliaMessageRepository messageRepository;
    private Listener listener;

    public GliaOnMessageUseCase(GliaMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void execute(Listener listener) {
        this.listener = listener;
        messageRepository.listenForMessages(this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            messageRepository.unregisterMessageListener(this);
            this.listener = null;
        }
    }

    @Override
    public void onMessage(ChatMessage chatMessage) {
        if (this.listener != null) {
            listener.onMessage(chatMessage);
        }
    }
}
