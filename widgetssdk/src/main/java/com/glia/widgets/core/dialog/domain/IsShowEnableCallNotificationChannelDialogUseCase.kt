package com.glia.widgets.core.dialog.domain

import com.glia.widgets.core.dialog.PermissionDialogManager
import com.glia.widgets.core.permissions.PermissionManager

internal interface IsShowEnableCallNotificationChannelDialogUseCase {
    operator fun invoke(): Boolean
}

internal class IsShowEnableCallNotificationChannelDialogUseCaseImpl(
    private val permissionManager: PermissionManager,
    private val permissionDialogManager: PermissionDialogManager
) : IsShowEnableCallNotificationChannelDialogUseCase {
    private val isCallNotificationChannelNotEnabled: Boolean get() = !permissionManager.hasCallNotificationChannelEnabled()
    private val hasNotShownCallNotificationNotEnabledRequest: Boolean get() = !permissionDialogManager.hasEnableCallNotificationChannelRequestShown()

    override fun invoke(): Boolean = isCallNotificationChannelNotEnabled && hasNotShownCallNotificationNotEnabledRequest
}
