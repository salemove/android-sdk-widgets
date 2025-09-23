package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.Media
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.logCallScreenButtonClicked

internal interface ToggleVisitorAudioMediaStateUseCase {
    operator fun invoke()
}

internal class ToggleVisitorAudioMediaStateUseCaseImpl(private val repository: EngagementRepository) : ToggleVisitorAudioMediaStateUseCase {
    override fun invoke() {
        val status: Media.Status = repository.visitorCurrentMediaState?.audio?.status ?: return

        when (status) {
            Media.Status.PLAYING -> {
                repository.muteVisitorAudio()
                GliaLogger.logCallScreenButtonClicked(ButtonNames.MUTE)
            }
            Media.Status.PAUSED -> {
                repository.unMuteVisitorAudio()
                GliaLogger.logCallScreenButtonClicked(ButtonNames.UNMUTE)
            }
            Media.Status.DISCONNECTED -> Logger.d(TAG, "Visitor Audio is disconnected")
        }

    }
}
