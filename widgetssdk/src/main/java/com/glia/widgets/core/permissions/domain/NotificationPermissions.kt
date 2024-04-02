package com.glia.widgets.core.permissions.domain

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import com.glia.widgets.core.permissions.PermissionManager
import com.glia.widgets.push.notifications.IsPushNotificationsSetUpUseCase

internal interface RequestNotificationPermissionIfPushNotificationsSetUpUseCase {
    operator fun invoke(doneCallback: () -> Unit)
}

internal class RequestNotificationPermissionIfPushNotificationsSetUpUseCaseImpl(
    private val permissionManager: PermissionManager,
    private val isNotificationPermissionGrantedUseCase: IsNotificationPermissionGrantedUseCase,
    private val isPushNotificationsSetUpUseCase: IsPushNotificationsSetUpUseCase
) : RequestNotificationPermissionIfPushNotificationsSetUpUseCase {
    @SuppressLint("InlinedApi")
    override fun invoke(doneCallback: () -> Unit) {
        if (!isPushNotificationsSetUpUseCase() || isNotificationPermissionGrantedUseCase()) {
            doneCallback()
            return
        }

        permissionManager.handlePermissions(
            additionalPermissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
            additionalPermissionsGrantedCallback = {
                doneCallback()
            }
        )
    }
}

internal interface IsNotificationPermissionGrantedUseCase {
    operator fun invoke(): Boolean
}

internal class IsNotificationPermissionGrantedUseCaseImpl(private val permissionManager: PermissionManager) : IsNotificationPermissionGrantedUseCase {
    override fun invoke(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || permissionManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
}
