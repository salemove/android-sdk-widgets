package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.ChatType
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.data.GliaChatRepository.HistoryLoadedListener
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository
import com.glia.widgets.core.engagement.domain.MapOperatorUseCase
import com.glia.widgets.core.engagement.domain.model.ChatMessageInternal
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.SingleEmitter

class GliaLoadHistoryUseCase(
    private val gliaChatRepository: GliaChatRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val engagementConfigRepository: GliaEngagementConfigRepository,
    private val mapOperatorUseCase: MapOperatorUseCase
) {

    fun execute(): Single<List<ChatMessageInternal>> {
        return loadHistory()
            .flatMapPublisher { Flowable.fromIterable(it.asIterable()) }
            .concatMapSingle(mapOperatorUseCase::execute)
            .toSortedList(Comparator.comparingLong { o: ChatMessageInternal ->
                o.chatMessage.timestamp
            })
    }

    private fun loadHistory() = Single.create { emitter: SingleEmitter<Array<ChatMessage>> ->
        loadHistory { messages: Array<ChatMessage>, error: Throwable? ->
            if (error != null) {
                emitter.onError(error)
            } else {
                emitter.onSuccess(messages)
            }
        }
    }

    private fun loadHistory(listener: HistoryLoadedListener) {
        if (engagementConfigRepository.chatType === ChatType.SECURE_MESSAGING) {
            secureConversationsRepository.fetchChatTranscript(listener)
        } else {
            gliaChatRepository.loadHistory(listener)
        }
    }
}