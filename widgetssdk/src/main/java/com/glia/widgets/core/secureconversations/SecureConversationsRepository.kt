package com.glia.widgets.core.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.MessageAttachment
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.chat.domain.GliaSendMessageUseCase
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

class SecureConversationsRepository(private val secureConversations: SecureConversations) {
    private val _messageSendingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)

    val messageSendingObservable: Observable<Boolean> = _messageSendingObservable

    fun fetchChatTranscript(listener: GliaChatRepository.HistoryLoadedListener) {
        secureConversations.fetchChatTranscript(listener::loaded)
    }

    fun send(message: String, queueIds: Array<String>, attachment: MessageAttachment, callback: RequestCallback<VisitorMessage?>) {
        _messageSendingObservable.onNext(true)
        secureConversations.send(message, queueIds, attachment, handleResult(callback))
    }

    fun send(message: String, queueIds: Array<String>, callback: RequestCallback<VisitorMessage?>) {
        _messageSendingObservable.onNext(true)
        secureConversations.send(message, queueIds, handleResult(callback))
    }

    fun send(message: String, queueIds: Array<String>, attachment: MessageAttachment, listener: GliaSendMessageUseCase.Listener) {
        send(message, queueIds, attachment) { visitorMessage, ex ->
            onMessageReceived(visitorMessage, ex, listener)
        }
    }

    fun send(message: String, queueIds: Array<String>, listener: GliaSendMessageUseCase.Listener) {
        send(message, queueIds) { visitorMessage, ex ->
            onMessageReceived(visitorMessage, ex, listener)
        }
    }

    fun markMessagesRead(callback: RequestCallback<Void>?) {
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
        listener: GliaSendMessageUseCase.Listener
    ) {
        if (ex != null)
            listener.error(ex)
        else
            listener.messageSent(visitorMessage)
    }

    fun getUnreadMessagesCount(callback: RequestCallback<Int>) =
        secureConversations.getUnreadMessageCount(callback)
}
