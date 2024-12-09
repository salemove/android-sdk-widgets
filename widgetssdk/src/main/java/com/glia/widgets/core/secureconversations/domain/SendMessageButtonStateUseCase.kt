package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.core.fileupload.FileAttachmentRepository
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.core.secureconversations.SendMessageRepository
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.messagecenter.MessageCenterState
import io.reactivex.rxjava3.core.Observable

internal class SendMessageButtonStateUseCase(
    private val sendMessageRepository: SendMessageRepository,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val secureConversationsRepository: SecureConversationsRepository,
    private val showMessageLimitErrorUseCase: ShowMessageLimitErrorUseCase,
    private val schedulers: Schedulers
) {

    operator fun invoke(): Observable<MessageCenterState.ButtonState> {
        val array = listOf(
            showMessageLimitObservable(),
            messageSendingObservable(),
            messageOrFilesReadyToSendObservable()
        )
        return Observable.combineLatest(array) { values ->
            val list = values.map { it as MessageCenterState.ButtonState }

            if (list.any { it == MessageCenterState.ButtonState.PROGRESS }) {
                return@combineLatest MessageCenterState.ButtonState.PROGRESS
            }
            if (list.any { it != MessageCenterState.ButtonState.NORMAL }) {
                return@combineLatest MessageCenterState.ButtonState.DISABLE
            }

            return@combineLatest MessageCenterState.ButtonState.NORMAL
        }
            .subscribeOn(schedulers.computationScheduler)
            .observeOn(schedulers.mainScheduler)
    }

    private fun messageSendingObservable() = secureConversationsRepository.messageSendingObservable
        .map { buttonProgressStateMap(it) }

    private fun showMessageLimitObservable() = showMessageLimitErrorUseCase()
        .map { buttonEnableStateMap(!it) }

    private fun messageOrFilesReadyToSendObservable(): Observable<MessageCenterState.ButtonState> {
        val array = listOf(
            messageIsNotEmptyObservable(),
            filesObservable()
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
        MessageCenterState.ButtonState.NORMAL
    } else {
        MessageCenterState.ButtonState.DISABLE
    }

    private fun buttonProgressStateMap(enable: Boolean) = if (enable) {
        MessageCenterState.ButtonState.PROGRESS
    } else {
        MessageCenterState.ButtonState.NORMAL
    }
}
