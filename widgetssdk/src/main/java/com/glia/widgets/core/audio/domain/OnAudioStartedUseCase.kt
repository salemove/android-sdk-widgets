package com.glia.widgets.core.audio.domain

import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.engagement.domain.OperatorMediaUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.hasAudio
import io.reactivex.Flowable

internal class OnAudioStartedUseCase(
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase
) {
    operator fun invoke(): Flowable<Boolean> {
        val array = arrayOf(
            operatorMediaObservable(),
            visitorMediaObservable()
        )
        return Flowable.combineLatest(array) {
            val isOperatorAudio = it[0] as Boolean
            val isVisitorAudio = it[1] as Boolean
            return@combineLatest isOperatorAudio || isVisitorAudio
        }.distinctUntilChanged()
    }

    private fun operatorMediaObservable() = visitorMediaUseCase().map(MediaState::hasAudio)

    private fun visitorMediaObservable() = operatorMediaUseCase().map(MediaState::hasAudio)
}
