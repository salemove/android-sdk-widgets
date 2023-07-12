package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import io.reactivex.Observable
import java.util.function.Consumer

internal class GliaOnMessageUseCase(
    private val messageRepository: GliaChatRepository,
    private val mapOperatorUseCase: MapOperatorUseCase
) {

    private val observable = Observable.create { observer ->
        val messageListener = Consumer<ChatMessage> { chatMessage ->
            observer.onNext(chatMessage)
        }

        messageRepository.listenForAllMessages(messageListener)

        observer.setCancellable {
            messageRepository.unregisterAllMessageListener(messageListener)
        }
    }
        .flatMapSingle { chatMessage: ChatMessage -> mapOperatorUseCase(chatMessage, isHistory = false, isLast = true) }
        .doOnError { obj: Throwable -> obj.printStackTrace() }
        .share()

    operator fun invoke(): Observable<ChatMessageInternal> = observable
}