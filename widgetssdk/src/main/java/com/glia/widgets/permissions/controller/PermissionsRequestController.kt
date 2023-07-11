package com.glia.widgets.permissions.controller

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.widgets.permissions.PermissionsRequestContract
import com.glia.widgets.permissions.PermissionsRequestRepository

internal class PermissionsRequestController(
    private val permissionsRequestRepository: PermissionsRequestRepository
) : PermissionsRequestContract.Controller, PermissionsRequestRepository.Launcher {

    init {
        permissionsRequestRepository.launcher = this
    }

    private lateinit var watcher: PermissionsRequestContract.Watcher

    @VisibleForTesting
    var permissionsRequest: Array<String>? = null

    @VisibleForTesting
    var requestHashCode: Int? = null

    override fun setWatcher(watcher: PermissionsRequestContract.Watcher) {
        this.watcher = watcher
    }

    override fun request(permissions: Array<String>) {
        if (watcher.hasCurrentActivity()) {
            launch(permissions)
        } else {
            permissionsRequest = permissions
        }
    }

    private fun launch(permissions: Array<String>) {
        if (watcher.currentActivityIsComponentActivity()) {
            watcher.requestPermissions(permissions)?.let { hashCode ->
                requestHashCode = hashCode
            } ?: run {
                permissionsRequest = permissions
            }
        } else {
            permissionsRequest = permissions
            watcher.openSupportActivity()
        }
    }

    override fun onActivityResumed() {
        permissionsRequest?.let {
            permissionsRequest = null
            launch(it)
        }
    }

    override fun onActivityDestroyed(hashCode: Int) {
        if (hashCode == requestHashCode) {
            permissionsRequestRepository.onRequestResult(
                null,
                GliaException(
                    "Activity responsible for the request has been destroyed",
                    GliaException.Cause.INTERNAL_ERROR
                )
            )
        }
    }

    override fun onRequestedPermissionsResult(permissionsResult: Map<String, Boolean>) {
        requestHashCode = null
        permissionsRequestRepository.onRequestResult(permissionsResult, null)
        if (!permissionsRequestRepository.hasPermissionRequest()) {
            watcher.destroySupportActivityIfExists()
        }
    }
}
