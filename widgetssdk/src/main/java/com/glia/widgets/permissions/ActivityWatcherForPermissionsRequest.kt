package com.glia.widgets.permissions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.glia.widgets.base.BaseActivityWatcher
import java.lang.ref.WeakReference

internal class ActivityWatcherForPermissionsRequest(
    private val controller: PermissionsRequestContract.Controller
) : BaseActivityWatcher(), PermissionsRequestContract.Watcher {

    init {
        controller.setWatcher(this)
    }

    private var currentActivity: WeakReference<Activity?> = WeakReference(null)

    private val permissionsLaunchers = mutableMapOf<Int, ActivityResultLauncher<Array<String>>>()

    override fun hasCurrentActivity(): Boolean = currentActivity.get() != null

    override fun currentActivityIsComponentActivity(): Boolean = currentActivity.get() is ComponentActivity

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)

        currentActivity = WeakReference(activity)
        registerForPermissionsResult(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)

        currentActivity = WeakReference(activity)
        controller.onActivityResumed()
    }

    override fun onActivityDestroyed(activity: Activity) {
        super.onActivityDestroyed(activity)

        if (activity == currentActivity.get()) {
            currentActivity = WeakReference(null)
        }
        val hashCode = System.identityHashCode(activity)
        permissionsLaunchers.remove(hashCode)?.unregister()
        controller.onActivityDestroyed(hashCode)
    }

    override fun requestPermissions(permissions: Array<String>): Int? {
        currentActivity.get()?.let {
            System.identityHashCode(it)
        }?.let { hashCode ->
            permissionsLaunchers[hashCode]?.let {
                it.launch(permissions)
                return hashCode
            }
        }
        return null
    }

    private fun registerForPermissionsResult(activity: Activity) {
        (activity as? ComponentActivity?)?.let { componentActivity ->
            val hashCode = System.identityHashCode(componentActivity)
            val permissionsLauncher = componentActivity
                .registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
                    controller.onRequestedPermissionsResult(permissionsResult)
                }
            permissionsLaunchers[hashCode] = permissionsLauncher
        }
    }

    override fun openSupportActivity() {
        currentActivity.get()?.run {
            val intent = Intent(this, PermissionsSupportActivity::class.java)
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }
    }

    override fun destroySupportActivityIfExists() {
        currentActivity.get()?.let { destroySupportActivityIfExists(it) }
    }

    private fun destroySupportActivityIfExists(activity: Activity) {
        if (activity is PermissionsSupportActivity) {
            activity.finish()
        }
    }
}
