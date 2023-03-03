package com.glia.widgets.callvisualizer.controller

import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository
import com.glia.widgets.core.dialog.DialogController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class VisitorCodeController(
    private val dialogController: DialogController,
    private val visitorCodeRepository: VisitorCodeRepository
    ) : VisitorCodeContract.Controller {

    private var disposable: Disposable? = null

    private lateinit var view: VisitorCodeContract.View

    override fun setView(view: VisitorCodeContract.View) {
        this.view = view
        this.view.notifySetupComplete()
    }

    override fun onCloseButtonClicked() {
        dialogController.dismissCurrentDialog()
    }

    override fun onLoadVisitorCode() {
        view.startLoading()
        disposable = visitorCodeRepository.getVisitorCode()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { visitorCode ->
                    view.showVisitorCode(visitorCode)
                    view.setTimer(visitorCode.duration)
                },
                { error ->
                    view.showError(error)
                }
            )
    }

    override fun onDestroy() {
        view.cleanUpOnDestroy()
    }
}
