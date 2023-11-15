package com.glia.widgets.callvisualizer.controller

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.callvisualizer.CallVisualizerCallback
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerEndUseCase
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.core.survey.OnSurveyListener
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class CallVisualizerController(
    private val callVisualizerRepository: CallVisualizerRepository,
    private val dialogController: DialogController,
    private val surveyUseCase: GliaSurveyUseCase,
    private val onCallVisualizerUseCase: GliaOnCallVisualizerUseCase,
    private val onCallVisualizerEndUseCase: GliaOnCallVisualizerEndUseCase,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    @get:VisibleForTesting val isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase
) : CallVisualizerCallback,
    GliaOnCallVisualizerUseCase.Listener,
    GliaOnCallVisualizerEndUseCase.Listener,
    OnSurveyListener {

    private var engagementEndedCallback: (() -> Unit)? = null
    private var startEngagementListener: ((OmnibrowseEngagement) -> Unit)? = null

    fun init() {
        Logger.d(TAG, "CallVisualizerController initialized")
        callVisualizerRepository.init(this)
        registerCallVisualizerListeners()
    }

    private fun registerCallVisualizerListeners() {
        onCallVisualizerUseCase(this) // newEngagementLoaded() callback
        onCallVisualizerEndUseCase.execute(this) // engagementEnded callback
    }

    override fun onOneWayMediaUpgradeRequest(
        mediaUpgradeOffer: MediaUpgradeOffer,
        operatorNameFormatted: String
    ) {
        dialogController.showUpgradeVideoDialog1Way(mediaUpgradeOffer, operatorNameFormatted)
    }

    override fun onTwoWayMediaUpgradeRequest(
        mediaUpgradeOffer: MediaUpgradeOffer,
        operatorNameFormatted: String
    ) {
        dialogController.showUpgradeVideoDialog2Way(mediaUpgradeOffer, operatorNameFormatted)
    }

    override fun onEngagementRequested() {
        dialogController.dismissVisitorCodeDialog()

        confirmationDialogUseCase { shouldShow ->
            if (shouldShow) {
                dialogController.showEngagementConfirmationDialog()
            } else {
                callVisualizerRepository.acceptEngagementRequest()
            }
        }
    }

    override fun onEngagementConfirmationDialogAllowed() {
        callVisualizerRepository.acceptEngagementRequest()
    }

    override fun onEngagementConfirmationDialogRejected() {
        callVisualizerRepository.declineEngagementRequest()
    }

    override fun onSurveyLoaded(survey: Survey?) {
        // Call Visualizer doesn't suppose to have a Survey,
        // so just destroying controllers
        surveyUseCase.unregisterListener(this)
        Dependencies.getControllerFactory().destroyControllers()
    }

    override fun newEngagementLoaded(engagement: OmnibrowseEngagement) {
        surveyUseCase.registerListener(this)
        startEngagementListener?.invoke(engagement)
    }

    override fun callVisualizerEngagementEnded() {
        engagementEndedCallback?.invoke()
        // Beware, this function is called before onSurveyLoaded()
        // No need to do anything currently
    }

    fun setOnEngagementEndedCallback(callback: () -> Unit) {
        this.engagementEndedCallback = callback
    }

    fun setOnEngagementStartListener(callback: (OmnibrowseEngagement) -> Unit) {
        startEngagementListener = callback
    }

    fun removeOnEngagementEndedCallback() {
        this.engagementEndedCallback = null
    }

    fun removeOnEngagementStartListener() {
        startEngagementListener = null
    }
}
