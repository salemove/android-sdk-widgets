package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaState
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.hasMedia
import io.reactivex.rxjava3.core.Flowable

internal interface OperatorMediaUseCase {
    val hasMedia: Boolean
    operator fun invoke(): Flowable<MediaState>
}

internal class OperatorMediaUseCaseImpl(private val engagementRepository: EngagementRepository) : OperatorMediaUseCase {
    private val operatorMediaState: Flowable<MediaState> = engagementRepository.operatorMediaState
        .filter(Data<MediaState>::hasValue)
        .map { it as Data.Value }
        .map(Data.Value<MediaState>::result)

    override val hasMedia: Boolean get() = engagementRepository.operatorCurrentMediaState?.hasMedia ?: false
    override fun invoke(): Flowable<MediaState> = operatorMediaState
}
