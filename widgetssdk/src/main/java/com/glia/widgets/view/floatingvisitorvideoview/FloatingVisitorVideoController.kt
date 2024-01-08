package com.glia.widgets.view.floatingvisitorvideoview

import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowOnHoldUseCase
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowVideoUseCase
import io.reactivex.disposables.CompositeDisposable

internal class FloatingVisitorVideoController(
    private val isShowVideoUseCase: IsShowVideoUseCase,
    private val isShowOnHoldUseCase: IsShowOnHoldUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase
) : FloatingVisitorVideoContract.Controller {
    private val disposables = CompositeDisposable()
    private var visitorMediaState: MediaState? = null
    private var isOnHold = false
    private var view: FloatingVisitorVideoContract.View? = null

    init {
        setup()
    }

    override fun onDestroy() {
        disposables.dispose()
    }

    override fun setView(view: FloatingVisitorVideoContract.View) {
        this.view = view
    }

    fun setup() {
        visitorMediaUseCase().unSafeSubscribe(::onNewVisitorMediaState)
        visitorMediaUseCase.onHoldState.unSafeSubscribe(::onHoldChanged)
    }

    private fun onNewVisitorMediaState(visitorMediaState: MediaState) {
        this.visitorMediaState = visitorMediaState
        disposables.add(
            isShowVideoUseCase.execute(visitorMediaState, isOnHold).subscribe({ showVisitorVideo(it, visitorMediaState) }) { }
        )
    }

    private fun showVisitorVideo(isShow: Boolean, mediaState: MediaState?) {
        if (isShow) {
            view!!.show(mediaState)
        } else {
            view!!.hide()
        }
    }

    private fun onHoldChanged(isOnHold: Boolean) {
        this.isOnHold = isOnHold
        disposables.add(
            isShowVideoUseCase.execute(visitorMediaState, isOnHold).subscribe({ showVisitorVideo(it, null) }) { }
        )
        disposables.add(
            isShowOnHoldUseCase.execute(isOnHold).subscribe({ showOnHold(it) }) { }
        )
    }

    private fun showOnHold(isShow: Boolean) {
        if (isShow) {
            view!!.showOnHold()
        } else {
            view!!.hideOnHold()
        }
    }
}
