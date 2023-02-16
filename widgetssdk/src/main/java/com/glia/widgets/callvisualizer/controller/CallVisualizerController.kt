package com.glia.widgets.callvisualizer.controller

import android.app.Activity
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.callvisualizer.CallVisualizerCallback
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerEndUseCase
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerUseCase
import com.glia.widgets.callvisualizer.domain.IsGliaActivityUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.survey.OnSurveyListener
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils

internal class CallVisualizerController(
    private val callVisualizerRepository: CallVisualizerRepository,
    private val dialogController: DialogController,
    private val surveyUseCase: GliaSurveyUseCase,
    private val onCallVisualizerUseCase: GliaOnCallVisualizerUseCase,
    private val onCallVisualizerEndUseCase: GliaOnCallVisualizerEndUseCase,
    private val isGliaActivityUseCase: IsGliaActivityUseCase
) : CallVisualizerCallback,
    GliaOnCallVisualizerUseCase.Listener,
    GliaOnCallVisualizerEndUseCase.Listener, OnSurveyListener {
    companion object {
        private val TAG = CallVisualizerController::class.java.simpleName
    }

    fun init() {
        Logger.d(TAG, "CallVisualizerController initialized")
        callVisualizerRepository.init(this)
        registerCallVisualizerListeners()
    }

    private fun registerCallVisualizerListeners() {
        onCallVisualizerUseCase.execute(this)    // newEngagementLoaded() callback
        onCallVisualizerEndUseCase.execute(this) // engagementEnded callback
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

    override fun onSurveyLoaded(survey: Survey?) {
        // Call Visualizer doesn't suppose to have a Survey,
        // so just destroying controllers
        surveyUseCase.unregisterListener(this)
        Dependencies.getControllerFactory().destroyControllers()
    }

    override fun newEngagementLoaded(engagement: OmnibrowseEngagement) {
        surveyUseCase.registerListener(this)
    }

    override fun engagementEnded() {
        // Beware, this function is called before onSurveyLoaded()
        // No need to do anything currently
    }
}
