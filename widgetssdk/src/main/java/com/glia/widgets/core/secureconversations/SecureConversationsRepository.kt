package com.glia.widgets.core.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.asStateFlowable
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

internal class SecureConversationsRepository(private val core: GliaCore, private val queueRepository: QueueRepository) {
    private val secureConversations: SecureConversations by lazy { core.secureConversations }

    private val _messageSendingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)
    val messageSendingObservable: Observable<Boolean> = _messageSendingObservable

    private val _unreadMessagesCountObservable: BehaviorProcessor<Int> = BehaviorProcessor.createDefault(0)
    val unreadMessagesCountObservable: Flowable<Int> get() = _unreadMessagesCountObservable.asStateFlowable()

    private val _pendingSecureConversationsStatusObservable: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
    val pendingSecureConversationsStatusObservable: Flowable<Boolean> get() = _pendingSecureConversationsStatusObservable.asStateFlowable()

    fun initialize() {
        secureConversations.apply {
            subscribeToUnreadMessageCount { count, _ -> count?.let(_unreadMessagesCountObservable::onNext) }
            subscribeToPendingSecureConversationStatus { hasPendingSecureConversations, _ ->
                hasPendingSecureConversations?.let(_pendingSecureConversationsStatusObservable::onNext)
            }
        }
    }

    fun fetchChatTranscript(listener: GliaChatRepository.HistoryLoadedListener) {
        secureConversations.fetchChatTranscript { messages, exception ->
            listener.loaded(messages?.toList(), exception)
        }
    }

    fun send(payload: SendMessagePayload, callback: RequestCallback<VisitorMessage?>) {
        _messageSendingObservable.onNext(true)
        queueRepository.relevantQueueIds.unSafeSubscribe { queueIds ->
            if (queueIds.isNotEmpty()) {
                secureConversations.send(payload, queueIds.toTypedArray(), handleResult(callback))
            } else {
                handleResult(callback).onResult(null, GliaException("relevant queues are empty", GliaException.Cause.INVALID_INPUT))
            }

        }
    }

    fun send(payload: SendMessagePayload, listener: GliaSendMessageUseCase.Listener) {
        send(payload) { visitorMessage, ex -> onMessageReceived(visitorMessage, ex, listener, payload) }
    }

    fun markMessagesRead(callback: RequestCallback<Void>) {
        secureConversations.markMessagesRead(callback)
    }

    private fun handleResult(callback: RequestCallback<VisitorMessage?>): RequestCallback<VisitorMessage?> {
        return RequestCallback { message: VisitorMessage?, exception: GliaException? ->
            _messageSendingObservable.onNext(false)
            callback.onResult(message, exception)
        }
    }

    private fun onMessageReceived(
        visitorMessage: VisitorMessage?,
        ex: GliaException?,
        listener: GliaSendMessageUseCase.Listener,
        payload: SendMessagePayload
    ) {
        if (ex != null) {
            listener.error(ex, payload.messageId)
        } else {
            listener.messageSent(visitorMessage)
        }
    }
}
