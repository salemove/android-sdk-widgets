package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.Media
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal interface ToggleVisitorVideoMediaStateUseCase {
    operator fun invoke()
}

internal class ToggleVisitorVideoMediaStateUseCaseImpl(private val repository: EngagementRepository) : ToggleVisitorVideoMediaStateUseCase {
    override fun invoke() {
        val status: Media.Status = repository.visitorCurrentMediaState?.video?.status ?: return
        when (status) {
            Media.Status.PLAYING -> repository.pauseVisitorVideo()
            Media.Status.PAUSED -> repository.resumeVisitorVideo()
            Media.Status.DISCONNECTED -> Logger.d(TAG, "Visitor Video is disconnected")
        }
    }
}
