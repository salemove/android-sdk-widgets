package com.glia.widgets.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import java.lang.ref.WeakReference

class ActivityWatcher(
    private val app: Application,
    private val callVisualizerController: CallVisualizerController
) :
    Application.ActivityLifecycleCallbacks {

    private var _resumedActivity: WeakReference<Activity?> = WeakReference(null)

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    @VisibleForTesting
    val resumedActivity: Activity?
        get() = _resumedActivity.get()

    fun init() {
        app.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}


    override fun onActivityResumed(activity: Activity) {
        _resumedActivity = WeakReference(activity)
        // There are separate handlers for Media requests on CallActivity and ChatActivity
        callVisualizerController.addDialogCallback(resumedActivity)
    }

    override fun onActivityPaused(activity: Activity) {
        _resumedActivity.clear()
        callVisualizerController.removeDialogCallback()
    }


    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}
