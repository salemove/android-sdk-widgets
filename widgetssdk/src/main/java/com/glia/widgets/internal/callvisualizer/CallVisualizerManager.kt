package com.glia.widgets.internal.callvisualizer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.SDK_TYPE
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeViewBuilderUseCase

internal class CallVisualizerManager(
    private val buildVisitorCodeUseCase: VisitorCodeViewBuilderUseCase,
    private val callVisualizerController: CallVisualizerContract.Controller
) : CallVisualizer {

    override fun createVisitorCodeView(context: Context): View {
        GliaLogger.logMethodUse(CallVisualizer::class, "createVisitorCodeView")
        return buildVisitorCodeUseCase(context, false)
    }

    override fun showVisitorCodeDialog() {
        GliaLogger.logMethodUse(CallVisualizer::class, "showVisitorCodeDialog")
        callVisualizerController.showVisitorCodeDialog()
    }

    override fun addVisitorContext(visitorContextAssetId: String) {
        GliaLogger.logMethodUse(CallVisualizer::class, "addVisitorContext")
        callVisualizerController.saveVisitorContextAssetId(visitorContextAssetId)
    }

    @SuppressLint("CheckResult")
    override fun onEngagementStart(runnable: Runnable) {
        GliaLogger.logMethodUse(CallVisualizer::class, "onEngagementStart")
        callVisualizerController.engagementStartFlow.subscribe { runnable.run() }
    }

    @SuppressLint("CheckResult")
    override fun onEngagementEnd(runnable: Runnable) {
        GliaLogger.logMethodUse(CallVisualizer::class, "onEngagementEnd")
        callVisualizerController.engagementEndFlow.subscribe { runnable.run() }
    }
}
