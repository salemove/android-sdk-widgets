package com.glia.widgets.view.floatingvisitorvideoview

import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowOnHoldUseCase
import com.glia.widgets.view.floatingvisitorvideoview.domain.IsShowVideoUseCase
import io.reactivex.disposables.CompositeDisposable

internal class FloatingVisitorVideoController(
    private val isShowVideoUseCase: IsShowVideoUseCase,
    private val isShowOnHoldUseCase: IsShowOnHoldUseCase
) : FloatingVisitorVideoContract.Controller {
    private val disposables = CompositeDisposable()
    private var view: FloatingVisitorVideoContract.View? = null
    override fun onResume() {
        disposables.add(
            isShowVideoUseCase.invoke()
                .doOnNext { value -> showVisitorVideo(value.first, value.second) }
                .subscribe()
        )
        disposables.add(
            isShowOnHoldUseCase.invoke()
                .doOnNext(::showOnHold)
                .subscribe()
        )
    }

    override fun onPause() {
        disposables.dispose()
    }

    override fun onDestroy() {}
    override fun setView(view: FloatingVisitorVideoContract.View) {
        this.view = view
    }

    private fun showVisitorVideo(isShow: Boolean, mediaState: VisitorMediaState?) {
        if (isShow && mediaState != null) {
            view?.show(mediaState)
        } else {
            view?.hide()
        }
    }

    private fun showOnHold(isShow: Boolean) {
        if (isShow) {
            view?.showOnHold()
        } else {
            view?.hideOnHold()
        }
    }
}
