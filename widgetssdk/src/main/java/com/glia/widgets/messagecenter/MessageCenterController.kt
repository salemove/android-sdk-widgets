package com.glia.widgets.messagecenter

class MessageCenterController : MessageCenterContract.Controller {
    private var view: MessageCenterContract.View? = null

    override fun setView(view: MessageCenterContract.View) {
        this.view = view
    }

    override fun onCheckMessagesClicked() {
        view?.navigateToMessaging()
    }

    override fun onSendMessageClicked() {
        view?.navigateToMessaging()
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
