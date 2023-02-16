package com.glia.widgets.callvisualizer.controller

import android.app.Activity
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.callvisualizer.CallVisualizerCallback
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.callvisualizer.domain.IsGliaActivityUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils

internal class CallVisualizerController(
    private val callVisualizerRepository: CallVisualizerRepository,
    private val dialogController: DialogController,
    private val isGliaActivityUseCase: IsGliaActivityUseCase
) : CallVisualizerCallback {
    companion object {
        private val TAG = CallVisualizerController::class.java.simpleName
    }

    fun init() {
        Logger.d(TAG, "CallVisualizerController initialized")
        callVisualizerRepository.init(this)
    }

    fun isGliaActivity(activity: Activity?) = isGliaActivityUseCase(activity)

    override fun onOneWayMediaUpgradeRequest(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String) {
        val formattedOperatorName = Utils.formatOperatorName(operatorName)
        dialogController.showUpgradeVideoDialog2Way(mediaUpgradeOffer, formattedOperatorName)
    }

    override fun onTwoWayMediaUpgradeRequest(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String) {
        val formattedOperatorName = Utils.formatOperatorName(operatorName)
        dialogController.showUpgradeVideoDialog1Way(mediaUpgradeOffer, formattedOperatorName)
    }
}
