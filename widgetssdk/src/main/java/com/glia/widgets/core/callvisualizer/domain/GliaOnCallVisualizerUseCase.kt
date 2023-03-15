package com.glia.widgets.core.callvisualizer.domain

import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.core.engagement.GliaEngagementRepository
import com.glia.widgets.core.engagement.GliaEngagementStateRepository
import com.glia.widgets.core.operator.GliaOperatorMediaRepository
import com.glia.widgets.core.queue.GliaQueueRepository
import com.glia.widgets.core.visitor.GliaVisitorMediaRepository
import java.util.function.Consumer

class GliaOnCallVisualizerUseCase(
    private val gliaRepository: GliaEngagementRepository,
    private val operatorMediaRepository: GliaOperatorMediaRepository,
    private val gliaQueueRepository: GliaQueueRepository,
    private val gliaVisitorMediaRepository: GliaVisitorMediaRepository,
    private val gliaEngagementStateRepository: GliaEngagementStateRepository
) : Consumer<OmnibrowseEngagement> {
    interface Listener {
        fun newEngagementLoaded(engagement: OmnibrowseEngagement)
    }

    private var listener: Listener? = null
    fun execute(listener: Listener) {
        if (this.listener == listener) {
            // Already listening
            return
        }
        this.listener = listener
        gliaRepository.listenForOmnibrowseEngagement(this)
    }

    override fun accept(engagement: OmnibrowseEngagement) {
        operatorMediaRepository.onEngagementStarted(engagement)
        gliaVisitorMediaRepository.onEngagementStarted(engagement)
        gliaEngagementStateRepository.onEngagementStarted(engagement)
        gliaQueueRepository.onEngagementStarted()
        if (listener != null) {
            listener!!.newEngagementLoaded(engagement)
        }
    }

    fun unregisterListener(listener: Listener) {
        if (this.listener === listener) {
            gliaRepository.unregisterCallVisualizerEngagementListener(this)
            this.listener = null
        }
    }
}
