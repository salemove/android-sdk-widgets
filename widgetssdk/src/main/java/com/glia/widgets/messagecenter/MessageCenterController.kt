package com.glia.widgets.messagecenter

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.core.secureconversations.domain.SendSecureMessageUseCase

class MessageCenterController(
    private val sendSecureMessageUseCase: SendSecureMessageUseCase
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
        sendSecureMessageUseCase.execute(message, callback)
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

    override fun onDestroy() {}
}
