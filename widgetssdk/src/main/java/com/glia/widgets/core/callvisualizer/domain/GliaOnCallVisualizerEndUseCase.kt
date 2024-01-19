package com.glia.widgets.core.callvisualizer.domain

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.operator.GliaOperatorMediaRepository
import com.glia.widgets.core.survey.GliaSurveyRepository
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class GliaOnCallVisualizerEndUseCase(
    private val repository: CallVisualizerRepository,
    private val operatorMediaRepository: GliaOperatorMediaRepository,
    private val callVisualizerUseCase: GliaOnCallVisualizerUseCase,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val surveyRepository: GliaSurveyRepository,
    private val gliaVisitorMediaRepository: GliaVisitorMediaRepository
) : GliaOnCallVisualizerUseCase.Listener {

    interface Listener {
        fun callVisualizerEngagementEnded()
    }

    inner class EndCallVisualizerRunnable(private val engagement: Engagement) : Runnable {
        override fun run() {
            Logger.i(TAG, "Call visualizer engagement ended")
            surveyRepository.onEngagementEnded(engagement)
            listener?.callVisualizerEngagementEnded()
            operatorMediaRepository.onEngagementEnded(engagement)
            removeScreenSharingNotificationUseCase()
            callNotificationUseCase.removeAllNotifications()
            gliaVisitorMediaRepository.onEngagementEnded(engagement)
        }
    }

    private var listener: Listener? = null

    private var endCallVisualizerRunnable: Runnable? = null

    fun execute(listener: Listener) {
        if (this.listener === listener) {
            // Already listening
            return
        }
        this.listener = listener
        callVisualizerUseCase(this)
    }

    fun unregisterListener(listener: Listener) {
        if (this.listener == listener) {
            endCallVisualizerRunnable?.let { repository.unregisterEngagementEndListener(it) }
            callVisualizerUseCase.unregisterListener(this)
            endCallVisualizerRunnable = null
            this.listener = null
        }
    }

    override fun newEngagementLoaded(engagement: OmnibrowseEngagement) {
        EndCallVisualizerRunnable(engagement).let {
            repository.listenForEngagementEnd(
                engagement,
                it
            )
        }
    }
}