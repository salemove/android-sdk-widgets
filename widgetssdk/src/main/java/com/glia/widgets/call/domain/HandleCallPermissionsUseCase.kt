package com.glia.widgets.call.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizer
import com.glia.widgets.permissions.PermissionsGrantedCallback

internal class HandleCallPermissionsUseCase(
    private val isCurrentEngagementCallVisualizer: IsCurrentEngagementCallVisualizer,
    private val permissionManager: PermissionManager
) {
    operator fun invoke(mediaType: Engagement.MediaType, callback: PermissionsGrantedCallback) {
        val permissions = permissionManager.getPermissionsForEngagementMediaType(
            mediaType,
            isCurrentEngagementCallVisualizer()
        )
        permissionManager.handlePermissions(
            permissions.requiredPermissions,
            permissions.additionalPermissions,
            callback
        )
    }
}
