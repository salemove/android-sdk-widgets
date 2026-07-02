package com.glia.widgets.internal.secureconversations

import android.annotation.SuppressLint
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.androidsdk.secureconversations.SecureConversations
import com.glia.widgets.chat.data.GliaChatRepository
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.asStateFlowable
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.internal.queue.QueueRepository
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

internal class SecureConversationsRepository @JvmOverloads constructor(
    private val core: GliaCore,
    private val queueRepository: QueueRepository,
    private val schedulers: Schedulers = Dependencies.schedulers
) {
    private val secureConversations: SecureConversations by lazy { core.secureConversations }

    private val _messageSendingObservable: Subject<Boolean> = BehaviorSubject.createDefault(false)
    val messageSendingObservable: Observable<Boolean> = _messageSendingObservable

    private val _unreadMessagesCountObservable: BehaviorProcessor<Int> = BehaviorProcessor.createDefault(0)
    val unreadMessagesCountObservable: Flowable<Int> get() = _unreadMessagesCountObservable.asStateFlowable()

    private val unreadMessagesCountCallback: RequestCallback<Int> = RequestCallback { count, _ -> count?.let(_unreadMessagesCountObservable::onNext) }

    private val _pendingSecureConversationsStatusObservable: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
    val pendingSecureConversationsStatusObservable: Flowable<Boolean> get() = _pendingSecureConversationsStatusObservable.asStateFlowable()

    val hasPendingSecureConversations: Boolean get() = _pendingSecureConversationsStatusObservable.value ?: false

    private val pendingSecureConversationsCallback: RequestCallback<Boolean> = RequestCallback { hasPendingSecureConversations, _ ->
        hasPendingSecureConversations?.let(_pendingSecureConversationsStatusObservable::onNext)
    }

    private val _isLeaveSecureConversationDialogVisibleObservable: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
    val isLeaveSecureConversationDialogVisibleObservable: Flowable<Boolean> get() = _isLeaveSecureConversationDialogVisibleObservable.asStateFlowable()

    fun subscribe() {
        secureConversations.apply {
            subscribeToUnreadMessageCount(unreadMessagesCountCallback)
            subscribeToPendingSecureConversationStatus(pendingSecureConversationsCallback)
        }
    }

    fun unsubscribeAndResetData() {
        unsubscribe()

        _unreadMessagesCountObservable.onNext(0)
        _pendingSecureConversationsStatusObservable.onNext(false)
    }

    private fun unsubscribe() {
        secureConversations.apply {
            unSubscribeFromUnreadMessageCount(unreadMessagesCountCallback)
            unSubscribeFromPendingSecureConversationStatus(pendingSecureConversationsCallback)
        }
    }

    fun fetchChatTranscript(listener: GliaChatRepository.HistoryLoadedListener) {
        secureConversations.fetchChatTranscript { messages, exception ->
            listener.loaded(messages?.toList(), exception)
        }
    }

    @SuppressLint("CheckResult")
    private fun withQueues(onSuccess: (Array<String>) -> Unit, onFailure: (GliaException) -> Unit) {
        queueRepository.relevantQueueIds.subscribe { queueIds ->
            if (queueIds.isNotEmpty()) {
                onSuccess(queueIds.toTypedArray())
            } else {
                onFailure(GliaException("relevant queues are empty", GliaException.Cause.INVALID_INPUT))
            }
        }
    }

    private fun wrapFailure(onFailure: (GliaException) -> Unit): (GliaException) -> Unit = {
        _messageSendingObservable.onNext(false)
        runOnMain { onFailure(it) }
    }

    private fun wrapSuccess(onSuccess: () -> Unit): () -> Unit = {
        _messageSendingObservable.onNext(false)
        runOnMain { onSuccess() }
    }

    // Core no longer hops to the main thread before invoking send callbacks, so marshal them
    // back onto it for the UI-driving consumers and the message-sending state.
    private fun runOnMain(block: () -> Unit) {
        schedulers.mainScheduler.scheduleDirect { block() }
    }

    fun sendMessageWithAttachments(
        content: String,
        fileAttachments: List<String>,
        messageId: String,
        onSuccess: () -> Unit,
        onFailure: (GliaException) -> Unit
    ) {
        _messageSendingObservable.onNext(true)

        withQueues({
            secureConversations.sendMessageWithAttachments(
                content = content,
                fileAttachments = fileAttachments,
                queueIds = it,
                messageId = messageId,
                onSuccess = wrapSuccess(onSuccess),
                onFailure = wrapFailure(onFailure)
            )
        }, wrapFailure(onFailure))
    }

    fun sendMessage(content: String, messageId: String, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
        _messageSendingObservable.onNext(true)

        withQueues({
            secureConversations.sendMessage(
                content = content,
                queueIds = it,
                messageId = messageId,
                onSuccess = wrapSuccess(onSuccess),
                onFailure = wrapFailure(onFailure)
            )
        }, wrapFailure(onFailure))
    }

    fun sendSingleChoiceAttachment(
        singleChoiceAttachment: SingleChoiceAttachment,
        messageId: String,
        onSuccess: () -> Unit,
        onFailure: (GliaException) -> Unit
    ) {
        _messageSendingObservable.onNext(true)

        withQueues({
            secureConversations.sendSingleChoiceAttachment(
                singleChoiceAttachment = singleChoiceAttachment,
                queueIds = it,
                messageId = messageId,
                onSuccess = wrapSuccess(onSuccess),
                onFailure = wrapFailure(onFailure)
            )
        }, wrapFailure(onFailure))
    }

    fun sendFileAttachment(fileId: String, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
        _messageSendingObservable.onNext(true)

        withQueues({
            secureConversations.sendFileAttachment(
                queueIds = it,
                fileId = fileId,
                onSuccess = wrapSuccess(onSuccess),
                onFailure = wrapFailure(onFailure)
            )
        }, wrapFailure(onFailure))
    }

    fun markMessagesRead(callback: RequestCallback<Void>) {
        secureConversations.markMessagesRead(callback)
    }

    fun setLeaveSecureConversationDialogVisible(visible: Boolean) {
        _isLeaveSecureConversationDialogVisibleObservable.onNext(visible)
    }
}
