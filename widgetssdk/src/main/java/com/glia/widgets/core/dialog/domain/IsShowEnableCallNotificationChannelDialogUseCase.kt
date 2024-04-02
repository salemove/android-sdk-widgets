package com.glia.widgets.core.dialog.domain

import com.glia.widgets.core.dialog.PermissionDialogManager
import com.glia.widgets.core.permissions.domain.HasCallNotificationChannelEnabledUseCase

internal interface IsShowEnableCallNotificationChannelDialogUseCase {
    operator fun invoke(): Boolean
}

internal class IsShowEnableCallNotificationChannelDialogUseCaseImpl(
    private val hasCallNotificationChannelEnabledUseCase: HasCallNotificationChannelEnabledUseCase,
    private val permissionDialogManager: PermissionDialogManager
) : IsShowEnableCallNotificationChannelDialogUseCase {
    private val isCallNotificationChannelNotEnabled: Boolean get() = !hasCallNotificationChannelEnabledUseCase()
    private val hasNotShownCallNotificationNotEnabledRequest: Boolean get() = !permissionDialogManager.hasEnableCallNotificationChannelRequestShown()

    override fun invoke(): Boolean = isCallNotificationChannelNotEnabled && hasNotShownCallNotificationNotEnabledRequest
}
