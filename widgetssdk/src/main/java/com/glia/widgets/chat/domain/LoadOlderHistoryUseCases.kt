package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatHistory
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.internal.engagement.domain.MapOperatorUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatHistoryResponse
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Loads the older page of chat history that a previous load's [ChatHistory.OlderPage] points at.
 * Applies to live chat and secure conversations alike.
 */
internal class LoadOlderHistoryUseCase(
    private val gliaChatRepository: GliaChatRepository,
    private val mapOperatorUseCase: MapOperatorUseCase
) {
    /** Older page with operators mapped, sorted oldest first, plus the key of the page before it. */
    operator fun invoke(olderPage: ChatHistory.OlderPage): Single<ChatHistoryResponse> {
        GliaLogger.i(LogEvents.CHAT_SCREEN_HISTORY_LOADING)
        return loadOlderHistory(olderPage)
            .flatMap { history ->
                Flowable.fromIterable(history.messages)
                    .concatMapSingle { mapOperatorUseCase(chatMessage = it) }
                    .toSortedList(Comparator.comparingLong { it.chatMessage.timestamp })
                    .map { ChatHistoryResponse(it, olderPage = history.olderPage) }
            }
            .doOnSuccess {
                GliaLogger.i(LogEvents.CHAT_SCREEN_HISTORY_LOADED) {
                    put(EventAttribute.MessageCount, it.items.size.toString())
                }
            }
    }

    private fun loadOlderHistory(olderPage: ChatHistory.OlderPage): Single<ChatHistory> = Single.create { emitter ->
        gliaChatRepository.loadOlderHistory(olderPage, emitter::onSuccess, emitter::onError)
    }
}
