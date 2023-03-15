package com.glia.widgets.core.callvisualizer.domain

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.callvisualizer.CallVisualizerRepository
import com.glia.widgets.core.notification.domain.RemoveCallNotificationUseCase
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.operator.GliaOperatorMediaRepository
import com.glia.widgets.core.survey.GliaSurveyRepository
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository

class GliaOnCallVisualizerEndUseCase(
    private val repository: CallVisualizerRepository,
    private val operatorMediaRepository: GliaOperatorMediaRepository,
    private val callVisualizerUseCase: GliaOnCallVisualizerUseCase,
    private val removeCallNotificationUseCase: RemoveCallNotificationUseCase,
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val surveyRepository: GliaSurveyRepository,
    private val gliaVisitorMediaRepository: GliaVisitorMediaRepository
) : GliaOnCallVisualizerUseCase.Listener {

    interface Listener {
        fun engagementEnded()
    }

    inner class EndCallVisualizerRunnable(private val engagement: Engagement) : Runnable {
        override fun run() {
            surveyRepository.onEngagementEnded(engagement)
            listener?.engagementEnded()
            operatorMediaRepository.stopListening(engagement)
            removeScreenSharingNotificationUseCase.execute()
            removeCallNotificationUseCase.execute()
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
        callVisualizerUseCase.execute(this)
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
