package com.glia.widgets.internal.dialog.domain

import com.glia.widgets.internal.dialog.PermissionDialogManager

internal interface SetOverlayPermissionRequestDialogShownUseCase {
    operator fun invoke()
}

internal class SetOverlayPermissionRequestDialogShownUseCaseImpl(private val permissionDialogManager: PermissionDialogManager) :
    SetOverlayPermissionRequestDialogShownUseCase {
    override fun invoke() = permissionDialogManager.setOverlayPermissionDialogShown()
}
