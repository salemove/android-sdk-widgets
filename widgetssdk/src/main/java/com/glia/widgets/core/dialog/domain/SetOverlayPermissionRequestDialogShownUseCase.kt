package com.glia.widgets.core.dialog.domain

import com.glia.widgets.core.dialog.PermissionDialogManager

internal interface SetOverlayPermissionRequestDialogShownUseCase {
    operator fun invoke()
}

internal class SetOverlayPermissionRequestDialogShownUseCaseImpl(private val permissionDialogManager: PermissionDialogManager) :
    SetOverlayPermissionRequestDialogShownUseCase {
    override fun invoke() = permissionDialogManager.setOverlayPermissionDialogShown()
}
