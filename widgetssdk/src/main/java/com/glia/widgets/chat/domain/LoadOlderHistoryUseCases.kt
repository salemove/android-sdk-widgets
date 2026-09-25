package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.internal.engagement.domain.MapOperatorUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

/**
 * Loads the next older page of chat history. The cursor lives in Core, so this applies to live
 * chat and secure conversations alike; the page is empty when there is nothing older.
 */
internal class LoadOlderHistoryUseCase(
    private val gliaChatRepository: GliaChatRepository,
    private val mapOperatorUseCase: MapOperatorUseCase
) {
    /** Older page with operators mapped, sorted oldest first. */
    operator fun invoke(): Single<List<ChatMessageInternal>> {
        GliaLogger.i(LogEvents.CHAT_SCREEN_HISTORY_LOADING)
        return loadOlderHistory()
            .flatMapPublisher { Flowable.fromIterable(it) }
            .concatMapSingle { mapOperatorUseCase(chatMessage = it) }
            .toSortedList(Comparator.comparingLong { it.chatMessage.timestamp })
            .map<List<ChatMessageInternal>> { it }
            .doOnSuccess {
                GliaLogger.i(LogEvents.CHAT_SCREEN_HISTORY_LOADED) {
                    put(EventAttribute.MessageCount, it.size.toString())
                }
            }
    }

    private fun loadOlderHistory(): Single<List<ChatMessage>> = Single.create { emitter ->
        gliaChatRepository.loadOlderHistory { messages, error ->
            error?.also { emitter.onError(it) } ?: emitter.onSuccess(messages.orEmpty())
        }
    }
}

/** Whether Core still holds an older page after the most recent history load. */
internal class HasOlderHistoryUseCase(private val gliaChatRepository: GliaChatRepository) {
    operator fun invoke(): Boolean = gliaChatRepository.hasOlderHistory()
}
