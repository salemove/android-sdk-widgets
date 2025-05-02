package com.glia.widgets.internal.engagement.domain

import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import io.reactivex.rxjava3.core.Single
import kotlin.jvm.optionals.getOrNull

internal class MapOperatorUseCase(private val getOperatorUseCase: GetOperatorUseCase) {
    operator fun invoke(chatMessage: ChatMessage): Single<ChatMessageInternal> =
        when (chatMessage.senderType) {
            Chat.Participant.OPERATOR -> processOperatorMessage(chatMessage as OperatorMessage)
            else -> processVisitorMessage(chatMessage)
        }

    private fun processOperatorMessage(chatMessage: OperatorMessage): Single<ChatMessageInternal> =
        getOperatorUseCase(chatMessage.operatorId!!).map { ChatMessageInternal(chatMessage, it.getOrNull()) }

    private fun processVisitorMessage(chatMessage: ChatMessage): Single<ChatMessageInternal> = Single.just(ChatMessageInternal(chatMessage))
}
