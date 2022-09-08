package com.glia.widgets.chat.domain;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.chat.data.GliaChatRepository;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase;
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class GliaOnMessageUseCase implements
        GliaOnEngagementUseCase.Listener,
        GliaChatRepository.MessageListener {

    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final GliaChatRepository messageRepository;
    private final MapOperatorUseCase mapOperatorUseCase;
    private final PublishSubject<ChatMessage> publishSubject;

    public GliaOnMessageUseCase(
            GliaChatRepository messageRepository,
            GliaOnEngagementUseCase gliaOnEngagementUseCase,
            MapOperatorUseCase mapOperatorUseCase) {
        this.onEngagementUseCase = gliaOnEngagementUseCase;
        this.messageRepository = messageRepository;
        this.mapOperatorUseCase = mapOperatorUseCase;
        publishSubject = PublishSubject.create();
    }

    public Observable<ChatMessageInternal> execute() {
        this.onEngagementUseCase.execute(this);
        return publishSubject
                .flatMapSingle(mapOperatorUseCase::execute)
                .doOnError(Throwable::printStackTrace)
                .share();
    }

    public void unregisterListener() {
            messageRepository.unregisterMessageListener(this);
            onEngagementUseCase.unregisterListener(this);
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        messageRepository.listenForMessages(this, engagement);
    }

    @Override
    public void onMessage(ChatMessage chatMessage) {
        publishSubject.onNext(chatMessage);
    }
}
