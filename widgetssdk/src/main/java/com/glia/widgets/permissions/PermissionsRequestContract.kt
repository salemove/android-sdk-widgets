package com.glia.widgets.permissions

class PermissionsRequestContract {
    interface Controller {
        fun setWatcher(watcher: Watcher)
        fun onActivityResumed()
        fun onActivityDestroyed(hashCode: Int)
        fun onRequestedPermissionsResult(permissionsResult: Map<String, Boolean>)
    }

    interface Watcher {
        fun hasCurrentActivity(): Boolean
        fun currentActivityIsComponentActivity(): Boolean
        fun requestPermissions(permissions: Array<String>): Int?
        fun openSupportActivity()
        fun destroySupportActivityIfExists()
    }
}
