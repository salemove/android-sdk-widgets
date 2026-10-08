package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatHistory
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.internal.engagement.domain.MapOperatorUseCase
import com.glia.widgets.internal.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.internal.secureconversations.domain.ManageSecureMessagingStatusUseCase
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

internal class GliaLoadHistoryUseCase(
    private val gliaChatRepository: GliaChatRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val shouldUseSecureMessagingApis: ManageSecureMessagingStatusUseCase,
    private val mapOperatorUseCase: MapOperatorUseCase
) {

    private val isSecureEngagement get() = shouldUseSecureMessagingApis.shouldUseSecureMessagingEndpoints

    operator fun invoke(): Single<ChatHistoryResponse> {
        GliaLogger.i(LogEvents.CHAT_SCREEN_HISTORY_LOADING)
        val single = if (isSecureEngagement) {
            loadHistoryWithNewMessagesCount()
        } else {
            loadHistoryAndMapOperator()
        }
        return single
            .doOnSuccess {
                GliaLogger.i(LogEvents.CHAT_SCREEN_HISTORY_LOADED) {
                    put(EventAttribute.MessageCount, it.items.size.toString())
                }
            }
    }

    private fun loadHistoryWithNewMessagesCount(): Single<ChatHistoryResponse> = Single.zip(
        loadHistoryAndMapOperator(),
        secureConversationsRepository.unreadMessagesCountObservable.firstOrError()
    ) { response, count -> response.copy(newMessagesCount = count) }

    private fun loadHistoryAndMapOperator(): Single<ChatHistoryResponse> = loadHistory().flatMap { history ->
        Flowable.fromIterable(history.messages)
            .concatMapSingle { mapOperatorUseCase(chatMessage = it) }
            .toSortedList(Comparator.comparingLong { it.chatMessage.timestamp })
            .map { ChatHistoryResponse(it, olderPage = history.olderPage) }
    }

    private fun loadHistory(): Single<ChatHistory> = Single.create { emitter ->
        gliaChatRepository.loadHistory(emitter::onSuccess, emitter::onError)
    }
}
