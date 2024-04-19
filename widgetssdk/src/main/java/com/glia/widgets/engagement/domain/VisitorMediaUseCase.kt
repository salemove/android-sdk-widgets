package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.hasMedia
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.TimeUnit

internal interface VisitorMediaUseCase {
    val hasMedia: Boolean
    val onHoldState: Flowable<Boolean>
    operator fun invoke(): Flowable<MediaState>
}

internal class VisitorMediaUseCaseImpl(private val engagementRepository: EngagementRepository) : VisitorMediaUseCase {
    private val visitorMediaState: Flowable<MediaState> = engagementRepository.visitorMediaState
        .filter(Data<MediaState>::hasValue)
        .map { it as Data.Value }
        .map(Data.Value<MediaState>::result)

    override val onHoldState: Flowable<Boolean> = engagementRepository.onHoldState

    override val hasMedia: Boolean get() = engagementRepository.visitorCurrentMediaState?.hasMedia ?: false

    override fun invoke(): Flowable<MediaState> = visitorMediaState
        .throttleLatest(1000, TimeUnit.MILLISECONDS)
}
