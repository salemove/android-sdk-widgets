package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.rxjava3.core.Flowable

internal interface OperatorMediaUpgradeOfferUseCase {
    operator fun invoke(): Flowable<MediaUpgradeOfferData>
}

internal data class MediaUpgradeOfferData(
    val offer: MediaUpgradeOffer,
    val operatorName: String
)

internal class OperatorMediaUpgradeOfferUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val currentOperatorUseCase: CurrentOperatorUseCase
) : OperatorMediaUpgradeOfferUseCase {
    override fun invoke(): Flowable<MediaUpgradeOfferData> = engagementRepository
        .mediaUpgradeOffer
        .withLatestFrom(currentOperatorUseCase.formattedName, ::MediaUpgradeOfferData)

}
