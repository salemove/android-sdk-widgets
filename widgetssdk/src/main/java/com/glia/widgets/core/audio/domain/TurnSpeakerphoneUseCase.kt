package com.glia.widgets.core.audio.domain

import com.glia.widgets.core.audio.AudioControlManager

internal class TurnSpeakerphoneUseCase(private val audioControlManager: AudioControlManager) {
    operator fun invoke(on: Boolean) {
        if (on) {
            audioControlManager.turnOnSpeakerphone()
        } else {
            audioControlManager.turnOffSpeakerphone()
        }
    }
}
