package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaQuality
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.helper.isPoor
import io.reactivex.rxjava3.core.Flowable

internal interface IsMediaQualityPoorUseCase {
    operator fun invoke(): Flowable<Boolean>
}

internal class IsMediaQualityPoorUseCaseImpl(private val engagementRepository: EngagementRepository) : IsMediaQualityPoorUseCase {
    override fun invoke(): Flowable<Boolean> = engagementRepository.mediaQuality.distinctUntilChanged().map(MediaQuality::isPoor)
}
