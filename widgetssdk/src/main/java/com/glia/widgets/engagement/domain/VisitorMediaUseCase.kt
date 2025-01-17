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

internal interface VisitorMediaUseCase {
    val hasAudio: Boolean
    val hasVideo: Boolean
    val hasMedia: Boolean
    val onHoldState: Flowable<Boolean>
    operator fun invoke(): Flowable<MediaState>
}

internal class VisitorMediaUseCaseImpl(private val engagementRepository: EngagementRepository) : VisitorMediaUseCase {
    private val emptyMediaState: MediaState = object : MediaState {
        override fun getVideo(): Video? = null
        override fun getAudio(): Audio? = null
    }

    private val visitorMediaState: Flowable<MediaState> = engagementRepository
        .visitorMediaState
        .map { if (it is Data.Value) it.result else emptyMediaState }

    override val onHoldState: Flowable<Boolean> = engagementRepository.onHoldState
    override val hasAudio: Boolean get() = engagementRepository.visitorCurrentMediaState?.hasAudio ?: false
    override val hasVideo: Boolean get() = engagementRepository.visitorCurrentMediaState?.hasVideo ?: false

    override val hasMedia: Boolean get() = engagementRepository.visitorCurrentMediaState?.hasMedia ?: false

    override fun invoke(): Flowable<MediaState> = visitorMediaState
}
