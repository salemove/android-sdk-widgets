package com.glia.widgets.push.notifications

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.view.dialog.UiComponentsDispatcher

internal interface RequestPushNotificationDuringAuthenticationUseCase {
    operator fun invoke()
}

internal class RequestPushNotificationDuringAuthenticationUseCaseImpl(
    private val isPushNotificationsSetUpUseCase: IsPushNotificationsSetUpUseCase,
    private val uiComponentsDispatcher: UiComponentsDispatcher,
    private val permissionManager: PermissionManager,
    private val configurationManager: ConfigurationManager
) : RequestPushNotificationDuringAuthenticationUseCase {
    override fun invoke() {
        when {
            // Suppress permission request
            configurationManager.suppressPushNotificationsPermissionRequestDuringAuthentication -> Logger.i(
                TAG,
                "Skipping push notifications permission prompt during authentication. suppressPushNotificationsPermissionRequestDuringAuthentication = true."
            )


            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                // No need to request permission on Android versions below Tiramisu
                return
            }

            !isPushNotificationsSetUpUseCase() -> {
                // Push Notifications are not set up, no need to request permission
                return
            }

            permissionManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Permission already granted, no need to request again
                return
            }

            permissionManager.shouldShowPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale dialog
                uiComponentsDispatcher.showNotificationPermissionDialog(onAllow = {
                    Logger.i(
                        TAG,
                        "Visitor's decision for intermediate push notification permission is 'allowed'"
                    )
                    requestNotificationPermission()
                }, onCancel = {
                    Logger.i(
                        TAG,
                        "Visitor's decision for intermediate push notification permission is 'declined'"
                    )
                })
            }

            else -> {
                // Directly request permission
                requestNotificationPermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() = permissionManager.handlePermissions(
        additionalPermissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
        additionalPermissionsGrantedCallback = {
            Logger.i(
                TAG,
                "Visitor's decision for push notification permission during auth. is 'allowed=$it'"
            )
        }
    )
}
