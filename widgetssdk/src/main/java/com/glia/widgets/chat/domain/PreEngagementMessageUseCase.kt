package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import io.reactivex.Observable
import java.util.function.Consumer

internal class PreEngagementMessageUseCase(
    private val messageRepository: GliaChatRepository,
    private val engagementRepository: GliaEngagementRepository,
    private val onEngagementUseCase: GliaOnEngagementUseCase,
    private val mapOperatorUseCase: MapOperatorUseCase
) {

    fun execute(): Observable<ChatMessageInternal> {
        if (engagementRepository.hasOngoingEngagement()) {
            return Observable.empty()
        }
        return Observable.create { observer ->
            val messageListener = Consumer<ChatMessage> { chatMessage ->
                observer.onNext(chatMessage)
            }

            val engagementListener = GliaOnEngagementUseCase.Listener {
                observer.onComplete()
            }

            messageRepository.listenForAllMessages(messageListener)
            onEngagementUseCase.execute(engagementListener)

            observer.setCancellable {
                messageRepository.unregisterAllMessageListener(messageListener)
                onEngagementUseCase.unregisterListener(engagementListener)
            }
        }
            .flatMapSingle { chatMessage: ChatMessage -> mapOperatorUseCase(chatMessage) }
            .doOnError { obj: Throwable -> obj.printStackTrace() }
            .share()
    }
}
