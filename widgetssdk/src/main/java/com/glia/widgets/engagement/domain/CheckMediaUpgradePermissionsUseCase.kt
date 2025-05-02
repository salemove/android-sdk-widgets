package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.internal.permissions.PermissionManager

internal interface CheckMediaUpgradePermissionsUseCase {
    operator fun invoke(offer: MediaUpgradeOffer, callback: (granted: Boolean) -> Unit)
}

internal class CheckMediaUpgradePermissionsUseCaseImpl(private val permissionManager: PermissionManager) : CheckMediaUpgradePermissionsUseCase {
    override fun invoke(offer: MediaUpgradeOffer, callback: (granted: Boolean) -> Unit) {
        with(permissionManager) {
            getPermissionsForMediaUpgradeOffer(offer).also {
                handlePermissions(
                    necessaryPermissions = it.requiredPermissions,
                    additionalPermissions = it.additionalPermissions,
                    necessaryPermissionsGrantedCallback = callback
                )
            }
        }
    }

}
