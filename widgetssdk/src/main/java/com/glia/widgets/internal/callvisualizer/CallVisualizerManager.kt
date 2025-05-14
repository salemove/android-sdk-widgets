package com.glia.widgets.internal.callvisualizer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.callvisualizer.CallVisualizer
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeViewBuilderUseCase
import com.glia.widgets.helper.unSafeSubscribe

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
    override fun onEngagementStart(onComplete: OnComplete) {
        callVisualizerController.engagementStartFlow.unSafeSubscribe { onComplete.onComplete() }
    }

    @SuppressLint("CheckResult")
    override fun onEngagementEnd(onComplete: OnComplete) {
        callVisualizerController.engagementEndFlow.unSafeSubscribe { onComplete.onComplete() }
    }
}
