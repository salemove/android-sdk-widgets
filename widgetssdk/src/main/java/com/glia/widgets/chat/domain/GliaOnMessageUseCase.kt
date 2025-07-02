package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.helper.isValid
import com.glia.widgets.internal.engagement.domain.MapOperatorUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import io.reactivex.rxjava3.core.Observable
import java.util.function.Consumer

internal class GliaOnMessageUseCase(
    private val messageRepository: GliaChatRepository,
    private val mapOperatorUseCase: MapOperatorUseCase
) {

    private val observable = Observable.create { observer ->
        val messageListener = Consumer<ChatMessage> { observer.onNext(it) }

        messageRepository.listenForAllMessages(messageListener)

        observer.setCancellable { messageRepository.unregisterAllMessageListener(messageListener) }
    }
        .filter { it.isValid() }
        .flatMapSingle { mapOperatorUseCase(it) }
        .doOnError { it.printStackTrace() }
        .share()

    operator fun invoke(): Observable<ChatMessageInternal> = observable
}
