package com.glia.widgets.core.engagement.domain

import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import io.reactivex.Single

internal class MapOperatorUseCase(private val getOperatorUseCase: GetOperatorUseCase) {
    operator fun invoke(chatMessage: ChatMessage): Single<ChatMessageInternal> =
        when (chatMessage.senderType) {
            Chat.Participant.OPERATOR -> Single.just(chatMessage)
                .cast(OperatorMessage::class.java)
                .flatMap { mapOperator(it) }

            else -> Single.just(chatMessage).map { ChatMessageInternal(it) }
        }

    private fun mapOperator(operatorMessage: OperatorMessage): Single<ChatMessageInternal> {
        return getOperatorUseCase.execute(operatorMessage.operatorId!!)
            .map { map(operatorMessage, it.orElse(null)) }
    }

    private fun map(operatorMessage: OperatorMessage, operator: Operator?): ChatMessageInternal {
        return ChatMessageInternal(operatorMessage, operator)
    }
}
