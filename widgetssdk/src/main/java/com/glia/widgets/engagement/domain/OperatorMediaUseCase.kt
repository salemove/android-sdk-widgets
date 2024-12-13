package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.Video
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.hasAudio
import com.glia.widgets.helper.hasMedia
import com.glia.widgets.helper.hasVideo
import io.reactivex.rxjava3.core.Flowable

internal interface OperatorMediaUseCase {
    val hasAudio: Boolean
    val hasVideo: Boolean
    val hasMedia: Boolean
    operator fun invoke(): Flowable<MediaState>
}

internal class OperatorMediaUseCaseImpl(private val engagementRepository: EngagementRepository) : OperatorMediaUseCase {
    private val emptyMediaState: MediaState = object : MediaState {
        override fun getVideo(): Video? = null
        override fun getAudio(): Audio? = null
    }

    private val operatorMediaState: Flowable<MediaState> = engagementRepository
        .operatorMediaState
        .map { if (it is Data.Value) it.result else emptyMediaState }
    override val hasAudio: Boolean get() = engagementRepository.operatorCurrentMediaState?.hasAudio ?: false
    override val hasVideo: Boolean get() = engagementRepository.operatorCurrentMediaState?.hasVideo ?: false

    override val hasMedia: Boolean get() = engagementRepository.operatorCurrentMediaState?.hasMedia ?: false
    override fun invoke(): Flowable<MediaState> = operatorMediaState
}
