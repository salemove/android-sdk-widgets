package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.SecureFileAttachmentRepository
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.messagecenter.State
import io.reactivex.Observable

class SendMessageButtonStateUseCase(
    private val sendMessageRepository: SendMessageRepository,
    private val fileAttachmentRepository: SecureFileAttachmentRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val showMessageLimitErrorUseCase: ShowMessageLimitErrorUseCase,
    private val schedulers: Schedulers
) {

    operator fun invoke(): Observable<State.ButtonState> {
        val array = arrayOf(
            showMessageLimitObservable(),
            messageSendingObservable(),
            messageOrFilesReadyToSendObservable()
        )
        return Observable.combineLatest(array) { values ->
            val list = values.map { it as State.ButtonState }

            if (list.any { it == State.ButtonState.PROGRESS }) {
                return@combineLatest State.ButtonState.PROGRESS
            }
            if (list.any { it != State.ButtonState.NORMAL }) {
                return@combineLatest State.ButtonState.DISABLE
            }

            return@combineLatest State.ButtonState.NORMAL
        }
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)
    }

    private fun messageSendingObservable() = secureConversationsRepository.messageSendingObservable
        .map { buttonProgressStateMap(it) }

    private fun showMessageLimitObservable() = showMessageLimitErrorUseCase()
        .map { buttonEnableStateMap(!it) }

    private fun messageOrFilesReadyToSendObservable(): Observable<State.ButtonState> {
        val array = arrayOf(
            messageIsNotEmptyObservable(),
            filesObservable(),
        )
        return Observable.combineLatest(array) {
            val messageIsNotEmpty = it[0] as Boolean
            val filePair = it[1] as Pair<Boolean, Boolean>
            val filesNotEmpty = filePair.first
            val filesReadyToSend = filesNotEmpty && filePair.second

            if (messageIsNotEmpty && filesNotEmpty) {
                return@combineLatest filesReadyToSend
            }
            return@combineLatest messageIsNotEmpty || filesReadyToSend
        }.map(::buttonEnableStateMap)
    }

    private fun messageIsNotEmptyObservable() = sendMessageRepository.observable
        .map { it.isNotEmpty() }

    private fun filesObservable() = fileAttachmentRepository.observable
        .map { files ->
            val isNotEmpty = files.isNotEmpty()
            val readyToSend = !files.any { !it.isReadyToSend }

            Pair(isNotEmpty, readyToSend)
        }

    private fun buttonEnableStateMap(enable: Boolean) = if (enable) {
        State.ButtonState.NORMAL
    } else {
        State.ButtonState.DISABLE
    }

    private fun buttonProgressStateMap(enable: Boolean) = if (enable) {
        State.ButtonState.PROGRESS
    } else {
        State.ButtonState.NORMAL
    }
}
