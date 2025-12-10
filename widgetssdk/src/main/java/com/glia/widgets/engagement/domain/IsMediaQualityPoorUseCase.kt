package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaQuality
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.isPoor
import io.reactivex.rxjava3.core.Flowable

internal interface IsMediaQualityPoorUseCase {
    operator fun invoke(): Flowable<Boolean>
}

internal class IsMediaQualityPoorUseCaseImpl(private val engagementRepository: EngagementRepository) : IsMediaQualityPoorUseCase {
    override fun invoke(): Flowable<Boolean> = engagementRepository.mediaQuality
        .distinctUntilChanged()
        .doOnNext(this::logMediaQualityChange)
        .map(MediaQuality::isPoor)

    private fun logMediaQualityChange(quality: MediaQuality) {
        val value = when (quality) {
            MediaQuality.POOR -> com.glia.telemetry_lib.MediaQuality.POOR
            MediaQuality.GOOD -> com.glia.telemetry_lib.MediaQuality.GOOD
        }

        GliaLogger.i(LogEvents.MEDIA_QUALITY_CHANGED, null, mapOf(EventAttribute.Value to value))
    }
}
