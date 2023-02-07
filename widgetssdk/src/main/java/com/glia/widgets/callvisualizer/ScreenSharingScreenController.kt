package com.glia.widgets.callvisualizer

class ScreenSharingScreenController : ScreenSharingScreenContract.Controller {

    private var view: ScreenSharingScreenContract.View? = null

    override fun setView(view: ScreenSharingScreenContract.View) {
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
