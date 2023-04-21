package com.glia.widgets.callvisualizer

import com.glia.widgets.GliaWidgets

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
        view?.finish()
    }

    override fun onActivityCreate() {
        GliaWidgets.getCallVisualizer().onEngagementEnd { view?.finish() }
    }

    override fun onDestroy() {}
}
