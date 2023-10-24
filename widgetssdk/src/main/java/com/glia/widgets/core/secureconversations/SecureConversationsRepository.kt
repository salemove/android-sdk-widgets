package com.glia.widgets.core.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import com.glia.widgets.chat.model.SendMessagePayload
import com.glia.widgets.chat.model.Unsent
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

internal class SecureConversationsRepository(private val secureConversations: SecureConversations) {
    private val _messageSendingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)

    val messageSendingObservable: Observable<Boolean> = _messageSendingObservable

    fun fetchChatTranscript(listener: GliaChatRepository.HistoryLoadedListener) {
        secureConversations.fetchChatTranscript(listener::loaded)
    }

    fun send(payload: SendMessagePayload, queueIds: Array<String>, callback: RequestCallback<VisitorMessage?>) {
        _messageSendingObservable.onNext(true)
        secureConversations.send(payload.payload, queueIds, handleResult(callback))
    }

    fun send(payload: SendMessagePayload, queueIds: Array<String>, listener: GliaSendMessageUseCase.Listener) {
        send(payload, queueIds) { visitorMessage, ex ->
            onMessageReceived(visitorMessage, ex, listener, payload)
        }
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
            listener.error(ex, Unsent(payload = payload, error = ex))
        } else {
            listener.messageSent(visitorMessage)
        }
    }

    fun getUnreadMessagesCount(callback: RequestCallback<Int>) =
        secureConversations.getUnreadMessageCount(callback)
}
