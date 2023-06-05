package com.glia.widgets.core.permissions.domain

import com.glia.widgets.core.permissions.PermissionManager

internal class HasScreenSharingNotificationChannelEnabledUseCase(private val permissionManager: PermissionManager) {
    operator fun invoke(): Boolean = permissionManager.hasScreenSharingNotificationChannelEnabled()
}
