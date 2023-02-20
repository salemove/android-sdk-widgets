package com.glia.widgets.callvisualizer

class EndScreenSharingController : EndScreenSharingContract.Controller {

    private var view: EndScreenSharingContract.View? = null

    override fun setView(view: EndScreenSharingContract.View) {
        this.view = view
    }

    override fun onBackArrowClicked() {
        view?.finish()
    }

    override fun onEndScreenSharingButtonClicked() {
        view?.stopScreenSharing()
    }

    override fun onDestroy() {}
}
