package com.glia.widgets.internal.permissions.domain

import android.Manifest
import android.os.Build
import com.glia.widgets.internal.permissions.PermissionManager

internal interface WithReadWritePermissionsUseCase {
    operator fun invoke(permissionGrantedCallback: () -> Unit)
}

internal class WithReadWritePermissionsUseCaseImpl(private val permissionManager: PermissionManager) : WithReadWritePermissionsUseCase {
    override fun invoke(permissionGrantedCallback: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionGrantedCallback()
            return
        }

        permissionManager.handlePermissions(
            necessaryPermissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            necessaryPermissionsGrantedCallback = {
                if (it) permissionGrantedCallback()
            }
        )
    }

}
