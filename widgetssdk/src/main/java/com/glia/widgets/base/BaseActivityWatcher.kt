package com.glia.widgets.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference

internal open class BaseActivityWatcher : SimpleActivityLifecycleCallbacks() {

    private val _resumedActivities = object : ArrayList<WeakReference<Activity>>(1) {
        fun add(activity: Activity) {
            super.add(WeakReference(activity))
        }

        fun remove(activity: Activity) {
            for (i in lastIndex downTo 0) {
                if (activity == super.get(i).get()) {
                    super.removeAt(i)
                }
            }
        }

        fun getAll(): List<Activity> {
            val activityList = ArrayList<Activity>()
            var iActivity: Activity?
            for (i in lastIndex downTo 0) {
                iActivity = super.get(i).get()
                if (iActivity == null) {
                    super.removeAt(i)
                } else {
                    activityList.add(iActivity)
                }
            }
            return activityList
        }
    }
    val resumedActivities: List<Activity> get() = _resumedActivities.getAll()

    private val resumedActivitySubject: PublishSubject<List<Activity>> = PublishSubject.create()
    val topActivityObserver: Observable<Activity> = resumedActivitySubject
        .filter { list -> list.isNotEmpty() }
        .map { list -> list.last() }

    override fun onActivityResumed(activity: Activity) {
        _resumedActivities.add(activity)
        resumedActivitySubject.onNext(_resumedActivities.getAll())
    }

    override fun onActivityPaused(activity: Activity) {
        _resumedActivities.remove(activity)
        resumedActivitySubject.onNext(_resumedActivities.getAll())
    }
}

internal open class SimpleActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

}
