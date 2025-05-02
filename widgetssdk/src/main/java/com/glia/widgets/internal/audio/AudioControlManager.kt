package com.glia.widgets.internal.audio

import android.content.Context
import com.glia.widgets.internal.audio.domain.OnAudioStartedUseCase
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class AudioControlManager(private val context: Context) {
    private val audioSwitch: AudioSwitch by lazy {
        AudioSwitch(context)
    }

    private var audioDevices: List<AudioDevice> = emptyList()
    private val disposable = CompositeDisposable()

    fun init(onAudioStartedUseCase: OnAudioStartedUseCase) {
        disposable.add(
            onAudioStartedUseCase().subscribe {
                if (it) {
                    activate()
                } else {
                    deactivate()
                }
            }
        )
    }

    fun turnOnSpeakerphone() {
        audioDevices.find { it is AudioDevice.Speakerphone }?.let { audioSwitch.selectDevice(it) }
    }

    fun turnOffSpeakerphone() {
        // choose the most priority device
        audioDevices.firstOrNull()?.let { audioSwitch.selectDevice(it) }
    }

    fun dispose() {
        disposable.clear()
    }

    private fun activate() {
        audioSwitch.start(::audioDeviceChangeListener)
        audioSwitch.activate()
    }

    private fun deactivate() {
        turnOffSpeakerphone()
        audioSwitch.deactivate()
    }

    private fun audioDeviceChangeListener(devices: List<AudioDevice>, selectedDevice: AudioDevice?) {
        audioDevices = devices
    }
}
