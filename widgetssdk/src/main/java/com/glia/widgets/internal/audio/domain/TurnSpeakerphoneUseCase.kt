package com.glia.widgets.internal.audio.domain

import com.glia.widgets.internal.audio.AudioControlManager

internal class TurnSpeakerphoneUseCase(private val audioControlManager: AudioControlManager) {
    operator fun invoke(on: Boolean) {
        if (on) {
            audioControlManager.turnOnSpeakerphone()
        } else {
            audioControlManager.turnOffSpeakerphone()
        }
    }
}
