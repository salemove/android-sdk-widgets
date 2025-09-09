package com.glia.widgets.internal.callvisualizer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeViewBuilderUseCase

internal class CallVisualizerManager(
    private val buildVisitorCodeUseCase: VisitorCodeViewBuilderUseCase,
    private val callVisualizerController: CallVisualizerContract.Controller
) : CallVisualizer {

    override fun createVisitorCodeView(context: Context): View {
        return buildVisitorCodeUseCase(context, false)
    }

    override fun showVisitorCodeDialog() {
        callVisualizerController.showVisitorCodeDialog()
    }

    override fun addVisitorContext(visitorContextAssetId: String) {
        callVisualizerController.saveVisitorContextAssetId(visitorContextAssetId)
    }

    @SuppressLint("CheckResult")
    override fun onEngagementStart(runnable: Runnable) {
        callVisualizerController.engagementStartFlow.subscribe { runnable.run() }
    }

    @SuppressLint("CheckResult")
    override fun onEngagementEnd(runnable: Runnable) {
        callVisualizerController.engagementEndFlow.subscribe { runnable.run() }
    }
}
