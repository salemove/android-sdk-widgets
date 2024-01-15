package com.glia.widgets.core.callvisualizer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.glia.androidsdk.GliaException
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeViewBuilderUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.unSafeSubscribe

internal class CallVisualizerManager(
    private val buildVisitorCodeUseCase: VisitorCodeViewBuilderUseCase,
    private val callVisualizerController: CallVisualizerContract.Controller
) : CallVisualizer {
    private fun checkForProperInit() {
        if (Dependencies.getSdkConfigurationManager().companyName.isNullOrEmpty()) {
            throw GliaException(
                "companyName not set during GliaWidgets.init(GliaWidgetsConfig gliaWidgetsConfig)",
                GliaException.Cause.INVALID_INPUT
            )
        }
    }

    override fun createVisitorCodeView(context: Context): View {
        checkForProperInit()
        return buildVisitorCodeUseCase(context, false)
    }

    override fun showVisitorCodeDialog(context: Context) {
        checkForProperInit()
        Dependencies.getControllerFactory().dialogController.showVisitorCodeDialog()
    }

    override fun addVisitorContext(visitorContext: String) {
        callVisualizerController.saveVisitorContextAssetId(visitorContext)
    }

    @SuppressLint("CheckResult")
    override fun onEngagementStart(runnable: Runnable) {
        callVisualizerController.engagementStartFlow.unSafeSubscribe { runnable.run() }
    }

    @SuppressLint("CheckResult")
    override fun onEngagementEnd(runnable: Runnable) {
        callVisualizerController.engagementEndFlow.unSafeSubscribe { runnable.run() }
    }
}
