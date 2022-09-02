package com.glia.widgets.core.engagement.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.OperatorMessage;
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal;

import io.reactivex.Single;

public class MapOperatorUseCase {
    private final GetOperatorUseCase getOperatorUseCase;

    public MapOperatorUseCase(GetOperatorUseCase getOperatorUseCase) {
        this.getOperatorUseCase = getOperatorUseCase;
    }

    public Single<ChatMessageInternal> execute(ChatMessage chatMessage) {
        if (chatMessage.isOperator()) {
            return Single.just(chatMessage).cast(OperatorMessage.class).flatMap(this::mapOperator);
        } else {
            return Single.just(chatMessage).map(ChatMessageInternal::new);
        }
    }

    @NonNull
    private Single<ChatMessageInternal> mapOperator(OperatorMessage operatorMessage) {
        return getOperatorUseCase.execute(operatorMessage.getOperatorId())
                .map(operator -> map(operatorMessage, operator.orElse(null)));
    }

    @NonNull
    private ChatMessageInternal map(@NonNull OperatorMessage operatorMessage, @Nullable Operator operator) {
        return new ChatMessageInternal(operatorMessage, operator);
    }

}
