package com.glia.widgets.call.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.permissions.PermissionsGrantedCallback

internal class HandleCallPermissionsUseCase(
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val permissionManager: PermissionManager
) {
    operator fun invoke(mediaType: Engagement.MediaType, callback: PermissionsGrantedCallback) {
        val permissions = permissionManager.getPermissionsForEngagementMediaType(
            mediaType,
            isCurrentEngagementCallVisualizerUseCase()
        )
        permissionManager.handlePermissions(
            permissions.requiredPermissions,
            permissions.additionalPermissions,
            callback
        )
    }
}
