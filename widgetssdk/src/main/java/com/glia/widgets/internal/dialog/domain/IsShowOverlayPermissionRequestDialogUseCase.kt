package com.glia.widgets.internal.dialog.domain

import com.glia.widgets.internal.dialog.PermissionDialogManager
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager

internal interface IsShowOverlayPermissionRequestDialogUseCase {
    operator fun invoke(): Boolean
}

internal class IsShowOverlayPermissionRequestDialogUseCaseImpl(
    private val permissionManager: PermissionManager,
    private val permissionDialogManager: PermissionDialogManager,
    private val gliaSdkConfigurationManager: ConfigurationManager
) : IsShowOverlayPermissionRequestDialogUseCase {
    private val hasNoOverlayPermissions: Boolean get() = !permissionManager.hasOverlayPermission()
    private val hasNotShownOverlayPermissionRequest: Boolean get() = !permissionDialogManager.hasOverlayPermissionDialogShown()
    private val isUseOverlay: Boolean get() = gliaSdkConfigurationManager.enableBubbleOutsideApp

    override fun invoke(): Boolean = hasNoOverlayPermissions && hasNotShownOverlayPermissionRequest && isUseOverlay
}
