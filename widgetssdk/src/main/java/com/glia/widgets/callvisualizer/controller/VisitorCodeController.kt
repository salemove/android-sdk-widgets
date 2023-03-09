package com.glia.widgets.callvisualizer.controller

import com.glia.androidsdk.Engagement
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.engagement.GliaEngagementRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class VisitorCodeController(
    private val dialogController: DialogController,
    private val visitorCodeRepository: VisitorCodeRepository,
    private val engagementRepository: GliaEngagementRepository
    ) : VisitorCodeContract.Controller {

    private var disposable: Disposable? = null
    private val onEngagementStartConsumer: (Engagement) -> Unit = {
        dialogController.dismissVisitorCodeDialog()
    }

    private lateinit var view: VisitorCodeContract.View

    override fun setView(view: VisitorCodeContract.View) {
        this.view = view
        this.view.notifySetupComplete()
        this.autoCloseOnEngagement()
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

    private fun autoCloseOnEngagement() {
        if (engagementRepository.hasOngoingEngagement()) {
            // Normally there is no need for visitor code if there is already ongoing engagement.
            // But it also doesn't seem right to just close it if right away when it was open
            return
        }
        engagementRepository.listenForCallVisualizerEngagement(onEngagementStartConsumer)
    }

    override fun onDestroy() {
        engagementRepository.unregisterCallVisualizerEngagementListener(onEngagementStartConsumer)
        view.cleanUpOnDestroy()
    }
}
