package com.glia.widgets.messagecenter

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.core.secureconversations.domain.IsMessageCenterAvailableUseCase
import com.glia.widgets.core.secureconversations.domain.SendSecureMessageUseCase

class MessageCenterController(
    private val sendSecureMessageUseCase: SendSecureMessageUseCase,
    private val isMessageCenterAvailableUseCase: IsMessageCenterAvailableUseCase
) : MessageCenterContract.Controller {
    private var view: MessageCenterContract.View? = null
    private var state = State()

    override fun setView(view: MessageCenterContract.View) {
        this.view = view
        setState(state)
    }

    override fun onCheckMessagesClicked() {
        view?.navigateToMessaging()
    }

    override fun onMessageChanged(message: String) {
        val showLimitError = message.count() > MAX_MESSAGE_LENGTH
        if (state.showMessageLimitError != showLimitError) {
            setState(state.copy(showMessageLimitError = showLimitError))
        }

        val sendButtonState = if (message.isEmpty() || showLimitError) {
            State.ButtonState.DISABLE
        } else {
            State.ButtonState.NORMAL
        }
        if (state.sendMessageButtonState != sendButtonState) {
            setState(state.copy(sendMessageButtonState = sendButtonState))
        }
    }

    override fun onSendMessageClicked(message: String) {
        setState(state.copy(
            sendMessageButtonState = State.ButtonState.PROGRESS,
            messageEditTextEnabled = false,
            addAttachmentButtonEnabled = false
        ))
        view?.hideSoftKeyboard()
        val callback =
            RequestCallback { _: VisitorMessage?, gliaException: GliaException? ->
                handleSendMessageResult(gliaException)
            }
        sendSecureMessageUseCase.execute(message, callback)
    }

    @VisibleForTesting
    fun handleSendMessageResult(gliaException: GliaException?) {
        if (gliaException == null) {
            view?.showConfirmationScreen()
        } else {
            when (gliaException.cause) {
                GliaException.Cause.AUTHENTICATION_ERROR -> view?.showMessageCenterUnavailableDialog()
                GliaException.Cause.INTERNAL_ERROR -> view?.showUnexpectedErrorDialog()
                else -> {
                    view?.showUnexpectedErrorDialog()
                    setState(state.copy(
                        sendMessageButtonState = State.ButtonState.NORMAL,
                        messageEditTextEnabled = true,
                        addAttachmentButtonEnabled = true
                    ))
                }
            }
        }
    }

    override fun onBackArrowClicked() {
        view?.finish()
    }

    override fun onCloseButtonClicked() {
        view?.finish()
    }

    override fun onAddAttachmentButtonClicked() {
        view?.showAttachmentPopup()
    }

    override fun onGalleryClicked() {
        TODO("Not yet implemented")
    }

    override fun onBrowseClicked() {
        TODO("Not yet implemented")
    }

    override fun onTakePhotoClicked() {
        TODO("Not yet implemented")
    }

    override fun isMessageCenterAvailable(callback: RequestCallback<Boolean>) {
        isMessageCenterAvailableUseCase.execute(callback)
    }

    override fun onDestroy() {
        isMessageCenterAvailableUseCase.dispose()
    }

    private fun setState(state: State) {
        this.state = state
        this.view?.onStateUpdated(state)
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}
