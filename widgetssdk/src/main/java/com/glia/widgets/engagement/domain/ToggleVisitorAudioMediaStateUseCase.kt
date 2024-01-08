package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.Media
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal interface ToggleVisitorAudioMediaStateUseCase {
    operator fun invoke()
}

internal class ToggleVisitorAudioMediaStateUseCaseImpl(private val repository: EngagementRepository) : ToggleVisitorAudioMediaStateUseCase {
    override fun invoke() {
        val status: Media.Status = repository.visitorCurrentMediaState?.audio?.status ?: return
        when (status) {
            Media.Status.PLAYING -> repository.muteVisitorAudio()
            Media.Status.PAUSED -> repository.unMuteVisitorAudio()
            Media.Status.DISCONNECTED -> Logger.d(TAG, "Visitor Audio is disconnected")
        }
    }
}
