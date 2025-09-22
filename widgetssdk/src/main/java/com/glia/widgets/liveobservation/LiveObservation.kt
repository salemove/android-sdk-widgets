package com.glia.widgets.liveobservation

import com.glia.telemetry_lib.GliaLogger

/**
 * Class responsible for handling Live Observation.
 *
 * Live Observation displays visitor interactions with the app to the operator to help provide better support.
 * At the moment, Live Observation on Android is only supported during ongoing engagements.
 * For more information about Live Observation, see integration guides.
 */
interface LiveObservation {
    /**
     * Pauses the Live Observation stream that displays visitor interactions with the app to the operator.
     *
     * This method should only be used to prevent the disclosure of sensitive user data.
     */
    fun pause()


    /**
     * Resumes the Live Observation stream that displays visitor interactions with the app to the operator.
     *
     * This method should only be used after sensitive data is no longer visible on the screen.
     */
    fun resume()
}

/**
 * @hide
 */
class LiveObservationImpl(
    private val liveObservation: com.glia.androidsdk.liveobservation.LiveObservation
) : LiveObservation {

    override fun pause() {
        GliaLogger.logMethodUse(LiveObservation::class, "pause")
        liveObservation.pause()
    }

    override fun resume() {
        GliaLogger.logMethodUse(LiveObservation::class, "resume")
        liveObservation.resume()
    }

}
