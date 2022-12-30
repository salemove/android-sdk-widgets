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

    override fun setView(view: MessageCenterContract.View) {
        this.view = view
    }

    override fun onCheckMessagesClicked() {
        view?.navigateToMessaging()
    }

    override fun onSendMessageClicked(message: String) {
        val callback =
            RequestCallback { _: VisitorMessage?, gliaException: GliaException? ->
                handleSendMessageResult(gliaException)
            }
        sendSecureMessageUseCase.execute(message, callback)
    }

    @VisibleForTesting
    fun handleSendMessageResult(gliaException: GliaException?) {
        if (gliaException == null) {
            view?.navigateToMessaging()
        } else {
            when (gliaException.cause) {
                GliaException.Cause.AUTHENTICATION_ERROR -> view?.showMessageCenterUnavailableDialog()
                GliaException.Cause.INTERNAL_ERROR -> view?.showUnexpectedErrorDialog()
                else -> {
                    view?.showUnexpectedErrorDialog()
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
}
