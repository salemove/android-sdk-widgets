package com.glia.widgets.permissions

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.telemetry_lib.Attributes
import com.glia.telemetry_lib.DialogNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents

internal class PermissionsRequestRepository {
    var launcher: Launcher? = null

    @VisibleForTesting
    val requests = mutableListOf<Pair<List<String>, PermissionsRequestResult>>()

    @Synchronized
    fun requestPermissions(permissions: List<String>, callback: PermissionsRequestResult) {
        requests.add(Pair(permissions, callback))
        if (requests.size == 1) {
            launch(permissions)
        }
    }

    fun onRequestDialogShown(permissions: List<String>) {
        GliaLogger.i(LogEvents.DIALOG_SHOWN) {
            put(Attributes.DIALOG_NAME, DialogNames.PERMISSIONS_REQUEST)
        }
    }

    fun onRequestResult(results: Map<String, Boolean>?, exception: GliaException?) {
        GliaLogger.i(LogEvents.DIALOG_CLOSED) {
            put(Attributes.DIALOG_NAME, DialogNames.PERMISSIONS_REQUEST)
        }

        // remove request from the permissions queue
        // and return result to the request callback
        requests.removeFirstOrNull()?.second?.let { it(results, exception) }

        // launch next permission request if exist
        requests.firstOrNull()?.let { launch(it.first) }
    }

    fun hasPermissionRequest(): Boolean {
        return requests.isNotEmpty()
    }

    private fun launch(permissions: List<String>) {
        launcher?.request(permissions.toTypedArray())
    }

    fun shouldShowPermissionRationale(permission: String): Boolean = launcher?.shouldShowPermissionRationale(permission) == true

    interface Launcher {
        fun request(permissions: Array<String>)
        fun shouldShowPermissionRationale(permission: String): Boolean
    }
}
