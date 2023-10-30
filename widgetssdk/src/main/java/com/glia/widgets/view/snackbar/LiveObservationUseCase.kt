package com.glia.widgets.view.snackbar

import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.androidsdk.omnicore.OmnicoreEngagement
import com.glia.widgets.core.engagement.GliaEngagementRepository
import java.util.function.Consumer

internal class LiveObservationUseCase(private val repository: GliaEngagementRepository) {
    private var startCallback: Runnable? = null

    private val omnicoreConsumer: Consumer<OmnicoreEngagement> = Consumer {
        startCallback?.run()
    }
    private val callVisualizerConsumer: Consumer<OmnibrowseEngagement> = Consumer {
        startCallback?.run()
    }

    fun init() = registerCallbacks()

    operator fun invoke(startCallback: Runnable) {
        this.startCallback = startCallback
    }

    private fun registerCallbacks() {
        repository.listenForOmnicoreEngagement(omnicoreConsumer)
        repository.listenForCallVisualizerEngagement(callVisualizerConsumer)
    }

}
