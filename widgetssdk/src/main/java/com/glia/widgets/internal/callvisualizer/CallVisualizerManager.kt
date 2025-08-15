package com.glia.widgets.internal.callvisualizer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.SDK_TYPE
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeViewBuilderUseCase

internal class CallVisualizerManager(
    private val buildVisitorCodeUseCase: VisitorCodeViewBuilderUseCase,
    private val callVisualizerController: CallVisualizerContract.Controller
) : CallVisualizer {

    override fun createVisitorCodeView(context: Context): View {
        GliaLogger.logMethodUse(SDK_TYPE.WIDGETS_SDK, CallVisualizer::class, "createVisitorCodeView")
        return buildVisitorCodeUseCase(context, false)
    }

    override fun showVisitorCodeDialog() {
        GliaLogger.logMethodUse(SDK_TYPE.WIDGETS_SDK, CallVisualizer::class, "showVisitorCodeDialog")
        callVisualizerController.showVisitorCodeDialog()
    }

    override fun addVisitorContext(visitorContextAssetId: String) {
        GliaLogger.logMethodUse(SDK_TYPE.WIDGETS_SDK, CallVisualizer::class, "addVisitorContext")
        callVisualizerController.saveVisitorContextAssetId(visitorContextAssetId)
    }

    @SuppressLint("CheckResult")
    override fun onEngagementStart(runnable: Runnable) {
        GliaLogger.logMethodUse(SDK_TYPE.WIDGETS_SDK, CallVisualizer::class, "onEngagementStart")
        callVisualizerController.engagementStartFlow.unSafeSubscribe { runnable.run() }
    }

    @SuppressLint("CheckResult")
    override fun onEngagementEnd(runnable: Runnable) {
        GliaLogger.logMethodUse(SDK_TYPE.WIDGETS_SDK, CallVisualizer::class, "onEngagementEnd")
        callVisualizerController.engagementEndFlow.unSafeSubscribe { runnable.run() }
    }
}
