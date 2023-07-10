package com.glia.widgets.core.engagement.domain

import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.androidsdk.chat.OperatorMessage
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import io.reactivex.Single
import kotlin.jvm.optionals.getOrNull

internal class MapOperatorUseCase(private val getOperatorUseCase: GetOperatorUseCase) {
    @JvmOverloads
    operator fun invoke(chatMessage: ChatMessage, isHistory: Boolean = false, isLast: Boolean = false): Single<ChatMessageInternal> =
        when (chatMessage.senderType) {
            Chat.Participant.OPERATOR -> processOperatorMessage(chatMessage as OperatorMessage, isHistory, isLast)
            else -> processVisitorMessage(chatMessage, isHistory, isLast)
        }

    private fun processOperatorMessage(chatMessage: OperatorMessage, isHistory: Boolean, isLast: Boolean): Single<ChatMessageInternal> =
        getOperatorUseCase.execute(chatMessage.operatorId!!)
            .map { ChatMessageInternal(chatMessage, isHistory, isLast, it.getOrNull()) }

    private fun processVisitorMessage(chatMessage: ChatMessage, isHistory: Boolean, isLast: Boolean): Single<ChatMessageInternal> = Single.just(
        ChatMessageInternal(chatMessage, isHistory, isLast)
    )

}
