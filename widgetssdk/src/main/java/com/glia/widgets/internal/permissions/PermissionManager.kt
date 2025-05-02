package com.glia.widgets.internal.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.permissions.Permissions
import com.glia.widgets.permissions.PermissionsGrantedCallback
import com.glia.widgets.permissions.PermissionsRequestRepository
import com.glia.widgets.permissions.PermissionsRequestResult

internal typealias CheckSelfPermission = (context: Context, permission: String) -> Int

internal class PermissionManager(
    private val applicationContext: Context,
    private val checkSelfPermission: CheckSelfPermission,
    private val permissionsRequestRepository: PermissionsRequestRepository,
    private val sdkInt: Int
) {

    fun hasOverlayPermission(): Boolean = Settings.canDrawOverlays(applicationContext)

    fun hasPermission(permission: String) = checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("InlinedApi")
    fun getPermissionsForMediaUpgradeOffer(offer: MediaUpgradeOffer): Permissions {
        Logger.i(TAG, "Request permissions for media upgrade offer")
        val requiredPermissions = buildList {
            if (offer.video == MediaDirection.TWO_WAY) {
                add(Manifest.permission.CAMERA)
            }

            if (offer.audio == MediaDirection.TWO_WAY) {
                add(Manifest.permission.RECORD_AUDIO)
            }
        }

        val additionalPermissions = buildList {
            if (sdkInt > Build.VERSION_CODES.R && offer.audio == MediaDirection.TWO_WAY) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }

            if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        return Permissions(requiredPermissions, additionalPermissions)
    }

    @SuppressLint("InlinedApi")
    fun getPermissionsForEngagementMediaType(mediaType: MediaType, isCallVisualizer: Boolean): Permissions {
        val requiredPermissions = mutableListOf<String>()
        val additionalPermissions = mutableListOf<String>()
        if (mediaType == MediaType.VIDEO) {
            requiredPermissions.add(Manifest.permission.CAMERA)
        }
        if (!isCallVisualizer) {
            if (mediaType == MediaType.AUDIO || mediaType == MediaType.VIDEO) {
                requiredPermissions.add(Manifest.permission.RECORD_AUDIO)
            }
            if (sdkInt > Build.VERSION_CODES.R) {
                additionalPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            additionalPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return Permissions(requiredPermissions, additionalPermissions)
    }

    fun handlePermissions(
        necessaryPermissions: List<String>? = null,
        additionalPermissions: List<String>? = null,
        necessaryPermissionsGrantedCallback: PermissionsGrantedCallback? = null,
        additionalPermissionsGrantedCallback: PermissionsGrantedCallback? = null,
        callback: PermissionsRequestResult? = null
    ) {
        val missingNecessaryPermissions = necessaryPermissions
            ?.filter { !hasPermission(it) }
        val missingAdditionalPermissions = additionalPermissions
            ?.filter { !hasPermission(it) }

        if (missingNecessaryPermissions.isNullOrEmpty() && missingAdditionalPermissions.isNullOrEmpty()) {
            necessaryPermissionsGrantedCallback?.invoke(true)
            additionalPermissionsGrantedCallback?.invoke(true)
            return
        }

        requestGroupedPermissions(
            missingNecessaryPermissions,
            missingAdditionalPermissions,
            necessaryPermissionsGrantedCallback,
            additionalPermissionsGrantedCallback,
            callback
        )
    }

    @VisibleForTesting
    fun requestGroupedPermissions(
        necessaryPermissions: List<String>? = null,
        additionalPermissions: List<String>? = null,
        necessaryPermissionsGrantedCallback: PermissionsGrantedCallback? = null,
        additionalPermissionsGrantedCallback: PermissionsGrantedCallback? = null,
        callback: PermissionsRequestResult? = null
    ) {
        Logger.i(TAG, "Request permissions")
        val allPermissions = (necessaryPermissions ?: emptyList()) + (additionalPermissions ?: emptyList())

        if (allPermissions.isEmpty()) {
            necessaryPermissionsGrantedCallback?.invoke(true)
            additionalPermissionsGrantedCallback?.invoke(true)
            callback?.invoke(emptyMap(), null)
            return
        }

        requestPermissions(allPermissions) { result, exception ->
            if (result != null) {
                necessaryPermissionsGrantedCallback
                    ?.invoke(necessaryPermissions?.any { result[it] == false }?.not() ?: true)
                additionalPermissionsGrantedCallback
                    ?.invoke(additionalPermissions?.any { result[it] == false }?.not() ?: true)
            } else {
                necessaryPermissionsGrantedCallback?.invoke(false)
                additionalPermissionsGrantedCallback?.invoke(false)
            }

            callback?.invoke(result, exception)
        }
    }

    @VisibleForTesting
    fun requestPermissions(permissions: List<String>, callback: PermissionsRequestResult) {
        permissionsRequestRepository.requestPermissions(permissions, callback)
    }
}
