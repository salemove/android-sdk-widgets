package com.glia.widgets.core.engagement.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;

import java.util.Optional;

public class ChatMessageInternal {
    @NonNull
    private final ChatMessage chatMessage;
    @Nullable
    private final Operator operator;

    public ChatMessageInternal(@NonNull ChatMessage chatMessage, @Nullable Operator operator) {
        this.chatMessage = chatMessage;
        this.operator = operator;
    }

    public ChatMessageInternal(ChatMessage chatMessage) {
        this(chatMessage, null);
    }

    @NonNull
    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public Optional<Operator> getOperator() {
        return Optional.ofNullable(operator);
    }

    public Optional<String> getOperatorId() {
        return getOperator().map(Operator::getId);
    }

    public Optional<String> getOperatorName() {
        return getOperator().map(Operator::getName);
    }

    public Optional<String> getOperatorImageUrl() {
        return getOperator().map(Operator::getPicture).flatMap(Operator.Picture::getURL);
    }

    public boolean isNotVisitor() {
        return getChatMessage().getSenderType() != Chat.Participant.VISITOR;
    }
}
