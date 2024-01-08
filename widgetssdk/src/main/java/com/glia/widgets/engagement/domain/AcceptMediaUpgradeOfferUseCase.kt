package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.EngagementRepository
import io.reactivex.Flowable

internal interface AcceptMediaUpgradeOfferUseCase {
    val result: Flowable<MediaUpgradeOffer>
    val resultForCallVisualizer: Flowable<MediaUpgradeOffer>
    operator fun invoke(offer: MediaUpgradeOffer): PermissionManager
}

internal class AcceptMediaUpgradeOfferUseCaseImpl(
    private val engagementRepository: EngagementRepository,
    private val permissionManager: PermissionManager
) : AcceptMediaUpgradeOfferUseCase {
    override val result: Flowable<MediaUpgradeOffer> = engagementRepository.mediaUpgradeOfferAcceptResult
        .filter { it.isSuccess }
        .map { it.getOrThrow() }

    override val resultForCallVisualizer: Flowable<MediaUpgradeOffer> = result.filter { engagementRepository.isCallVisualizerEngagement }

    override fun invoke(offer: MediaUpgradeOffer): PermissionManager = permissionManager.apply {
        val permissions = getPermissionsForMediaUpgradeOffer(offer)
        handlePermissions(permissions.requiredPermissions, permissions.additionalPermissions, {
            if (it) engagementRepository.acceptMediaUpgradeRequest(offer)
        })
    }
}
