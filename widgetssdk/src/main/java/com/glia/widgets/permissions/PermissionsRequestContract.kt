package com.glia.widgets.permissions

internal class PermissionsRequestContract {
    interface Controller {
        fun setWatcher(watcher: Watcher)
        fun onActivityResumed()
        fun onActivityDestroyed(hashCode: Int)
        fun onRequestedPermissionsResult(permissionsResult: Map<String, Boolean>)
    }

    interface Watcher {
        fun hasValidActivity(): Boolean
        fun currentActivityIsComponentActivity(): Boolean
        fun requestPermissions(permissions: Array<String>): Int?
        fun openSupportActivity()
        fun destroySupportActivityIfExists()
        fun shouldShowPermissionRationale(permission: String): Boolean
    }
}
