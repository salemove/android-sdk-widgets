package com.glia.widgets.core.dialog.domain

import com.glia.widgets.core.dialog.PermissionDialogManager

internal interface SetEnableCallNotificationChannelDialogShownUseCase {
    operator fun invoke()
}

internal class SetEnableCallNotificationChannelDialogShownUseCaseImpl(private val permissionDialogManager: PermissionDialogManager) :
    SetEnableCallNotificationChannelDialogShownUseCase {
    override fun invoke() = permissionDialogManager.setEnableCallNotificationChannelRequestShown()
}
