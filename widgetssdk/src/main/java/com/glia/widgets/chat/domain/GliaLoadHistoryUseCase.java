package com.glia.widgets.chat.domain;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.chat.data.GliaChatRepository;
import com.glia.widgets.chat.model.history.ChatItem;
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase;
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal;

import java.util.Comparator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class GliaLoadHistoryUseCase {

    private final GliaChatRepository gliaChatRepository;
    private final MapOperatorUseCase mapOperatorUseCase;

    public GliaLoadHistoryUseCase(GliaChatRepository gliaChatRepository, MapOperatorUseCase mapOperatorUseCase) {
        this.gliaChatRepository = gliaChatRepository;
        this.mapOperatorUseCase = mapOperatorUseCase;
    }

    public Single<List<ChatMessageInternal>> execute() {
        return loadHistory().flatMapPublisher(Flowable::fromArray)
                .concatMapSingle(mapOperatorUseCase::execute)
                .toSortedList(Comparator.comparingLong(o -> o.getChatMessage().getTimestamp()));
    }

    private Single<ChatMessage[]> loadHistory() {
        return Single.create(emitter -> gliaChatRepository.loadHistory((messages, error) -> {
            if (error != null) {
                emitter.onError(error);
            } else {
                emitter.onSuccess(messages);
            }
        }));
    }

}
