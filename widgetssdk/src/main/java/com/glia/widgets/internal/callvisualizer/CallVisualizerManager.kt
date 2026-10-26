package com.glia.widgets.internal.callvisualizer

import android.content.Context
import android.view.View
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.callvisualizer.CallVisualizer
import com.glia.widgets.helper.runIntegratorCallback
import com.glia.widgets.internal.callvisualizer.domain.VisitorCodeViewBuilderUseCase
import io.reactivex.rxjava3.disposables.Disposable

internal class CallVisualizerManager(
    private val buildVisitorCodeUseCase: VisitorCodeViewBuilderUseCase,
    private val callVisualizerController: CallVisualizerContract.Controller
) : CallVisualizer {

    // A new callback replaces the previous one, as the public API promises. Disposing the old
    // subscription also releases whatever the old callback holds, such as a destroyed Fragment.
    private var engagementStartDisposable: Disposable? = null
    private var engagementEndDisposable: Disposable? = null

    override fun createVisitorCodeView(context: Context): View {
        GliaLogger.logMethodUse(CallVisualizer::class, "createVisitorCodeView")
        return buildVisitorCodeUseCase(context, false)
    }

    override fun showVisitorCodeDialog() {
        GliaLogger.logMethodUse(CallVisualizer::class, "showVisitorCodeDialog")
        callVisualizerController.showVisitorCodeDialog()
    }

    override fun hideVisitorCodeDialog() {
        GliaLogger.logMethodUse(CallVisualizer::class, "hideVisitorCodeDialog")
        callVisualizerController.dismissVisitorCodeDialog()
    }

    override fun addVisitorContext(visitorContextAssetId: String) {
        GliaLogger.logMethodUse(CallVisualizer::class, "addVisitorContext")
        callVisualizerController.saveVisitorContextAssetId(visitorContextAssetId)
    }

    override fun onEngagementStart(onComplete: OnComplete) {
        GliaLogger.logMethodUse(CallVisualizer::class, "onEngagementStart")
        engagementStartDisposable?.dispose()
        engagementStartDisposable = callVisualizerController.engagementStartFlow
            .subscribe { runIntegratorCallback { onComplete.onComplete() } }
    }

    override fun onEngagementEnd(onComplete: OnComplete) {
        GliaLogger.logMethodUse(CallVisualizer::class, "onEngagementEnd")
        engagementEndDisposable?.dispose()
        engagementEndDisposable = callVisualizerController.engagementEndFlow
            .subscribe { runIntegratorCallback { onComplete.onComplete() } }
    }
}
