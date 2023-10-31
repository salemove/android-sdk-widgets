package com.glia.widgets.call.domain

import com.glia.androidsdk.Engagement
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.permissions.PermissionsGrantedCallback

internal class HandleCallPermissionsUseCase(
    private val isCallVisualizerUseCase: IsCallVisualizerUseCase,
    private val permissionManager: PermissionManager
) {
    operator fun invoke(mediaType: Engagement.MediaType, callback: PermissionsGrantedCallback) {
        val permissions = permissionManager.getPermissionsForEngagementMediaType(
            mediaType,
            isCallVisualizerUseCase()
        )
        permissionManager.handlePermissions(
            permissions.requiredPermissions,
            permissions.additionalPermissions,
            callback
        )
    }
}
