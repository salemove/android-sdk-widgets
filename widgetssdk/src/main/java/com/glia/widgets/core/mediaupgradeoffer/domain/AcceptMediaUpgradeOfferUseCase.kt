package com.glia.widgets.core.mediaupgradeoffer.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository
import com.glia.widgets.core.permissions.PermissionManager

internal class AcceptMediaUpgradeOfferUseCase(
    private val mediaUpgradeOfferRepository: MediaUpgradeOfferRepository,
    private val permissionManager: PermissionManager
) {
    operator fun invoke(offer: MediaUpgradeOffer, submitter: MediaUpgradeOfferRepository.Submitter) {
        val permissions = permissionManager.getPermissionsForMediaUpgradeOffer(offer)
        permissionManager.handlePermissions(
            permissions.requiredPermissions,
            permissions.additionalPermissions,
            { isGranted ->
                if (isGranted) mediaUpgradeOfferRepository.acceptOffer(offer, submitter)
            }
        )
    }
}
