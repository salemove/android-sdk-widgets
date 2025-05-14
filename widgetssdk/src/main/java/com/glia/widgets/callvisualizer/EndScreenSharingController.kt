package com.glia.widgets.callvisualizer

import com.glia.widgets.GliaWidgets
import com.glia.widgets.engagement.domain.EndScreenSharingUseCase

internal class EndScreenSharingController(private val endScreenSharingUseCase: EndScreenSharingUseCase) : EndScreenSharingContract.Controller {

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

    override fun onForceStopScreenSharing() {
        endScreenSharingUseCase()
    }

    override fun onActivityCreate() {
        GliaWidgets.getCallVisualizer().onEngagementEnd(onComplete = { view?.finish() })
    }

    override fun onDestroy() {}
}
