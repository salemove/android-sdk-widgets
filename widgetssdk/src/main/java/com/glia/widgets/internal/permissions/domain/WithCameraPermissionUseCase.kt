package com.glia.widgets.internal.permissions.domain

import android.Manifest
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.permissions.PermissionsGrantedCallback

internal interface WithCameraPermissionUseCase {
    operator fun invoke(permissionGrantedCallback: () -> Unit)
}

internal class WithCameraPermissionUseCaseImpl(private val permissionManager: PermissionManager) : WithCameraPermissionUseCase {
    override fun invoke(permissionGrantedCallback: () -> Unit) = requestPermission {
        if (it) permissionGrantedCallback()
    }

    private fun requestPermission(permissionsGrantedCallback: PermissionsGrantedCallback) {
        permissionManager.handlePermissions(
            necessaryPermissions = listOf(Manifest.permission.CAMERA),
            necessaryPermissionsGrantedCallback = permissionsGrantedCallback
        )
    }
}
