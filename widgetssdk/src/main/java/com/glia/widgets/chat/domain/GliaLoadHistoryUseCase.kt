package com.glia.widgets.chat.domain

import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase
import com.glia.widgets.core.engagement.domain.model.ChatHistoryResponse
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.domain.GetUnreadMessagesCountWithTimeoutUseCase
import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

internal class GliaLoadHistoryUseCase(
    private val gliaChatRepository: GliaChatRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val shouldUseSecureMessagingApis: ManageSecureMessagingStatusUseCase,
    private val mapOperatorUseCase: MapOperatorUseCase,
    private val getUnreadMessagesCountUseCase: GetUnreadMessagesCountWithTimeoutUseCase
) {

    private val isSecureEngagement get() = shouldUseSecureMessagingApis.shouldUseSecureMessagingEndpoints

    operator fun invoke(): Single<ChatHistoryResponse> = if (isSecureEngagement) {
        loadHistoryWithNewMessagesCount()
    } else {
        loadHistoryAndMapOperator().map { ChatHistoryResponse(it) }
    }

    private fun loadHistoryWithNewMessagesCount() = Single.zip(
        loadHistoryAndMapOperator(),
        getUnreadMessagesCountUseCase()
    ) { messages, count -> ChatHistoryResponse(messages, count) }

    private fun loadHistoryAndMapOperator(): Single<MutableList<ChatMessageInternal>> = loadHistory()
        .flatMapPublisher { Flowable.fromIterable(it) }
        .concatMapSingle { mapOperatorUseCase(chatMessage = it) }
        .toSortedList(Comparator.comparingLong { it.chatMessage.timestamp })

    private fun loadHistory() = Single.create { emitter ->
        loadHistory { messages, error ->
            error?.also { emitter.onError(it) } ?: emitter.onSuccess(messages)
        }
    }

    private fun loadHistory(listener: GliaChatRepository.HistoryLoadedListener) {
        if (isSecureEngagement) {
            secureConversationsRepository.fetchChatTranscript(listener)
        } else {
            gliaChatRepository.loadHistory(listener)
        }
    }
}
