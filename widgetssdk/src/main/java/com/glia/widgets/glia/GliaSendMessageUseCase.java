package com.glia.widgets.glia;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.widgets.model.GliaMessageRepository;

public class GliaSendMessageUseCase implements RequestCallback<VisitorMessage> {

    public interface Listener {
        void messageSent(VisitorMessage message, GliaException exception);
    }

    private final GliaMessageRepository repository;
    private Listener listener;

    public GliaSendMessageUseCase(GliaMessageRepository repository) {
        this.repository = repository;
    }

    public void execute(String message, Listener listener) {
        this.listener = listener;
        repository.sendMessage(message, this);
    }

    public void execute(SingleChoiceAttachment singleChoiceAttachment, Listener listener){
        repository.sendMessage(singleChoiceAttachment, this);
    }

    public void unregisterListener(Listener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public void onResult(VisitorMessage visitorMessage, GliaException e) {
        if (this.listener != null) {
            listener.messageSent(visitorMessage, e);
        }
    }
}
