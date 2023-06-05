package com.glia.widgets.core.permissions.domain

import com.glia.widgets.core.permissions.PermissionManager

internal class HasCallNotificationChannelEnabledUseCase(private val permissionManager: PermissionManager) {
    operator fun invoke(): Boolean = permissionManager.hasCallNotificationChannelEnabled()
}
