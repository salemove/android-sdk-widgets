package com.glia.widgets.core.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.SendMessagePayload
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.core.queue.QueueRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

internal class SecureConversationsRepository(private val secureConversations: SecureConversations, private val queueRepository: QueueRepository) {
    private val _messageSendingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)

    val messageSendingObservable: Observable<Boolean> = _messageSendingObservable

    fun fetchChatTranscript(listener: GliaChatRepository.HistoryLoadedListener) {
        secureConversations.fetchChatTranscript(listener::loaded)
    }

    fun send(payload: SendMessagePayload, callback: RequestCallback<VisitorMessage?>) {
        _messageSendingObservable.onNext(true)
        secureConversations.send(payload, queueRepository.relevantQueueIds.toTypedArray(), handleResult(callback))
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

    fun getUnreadMessagesCount(callback: RequestCallback<Int>) =
        secureConversations.getUnreadMessageCount(callback)
}
