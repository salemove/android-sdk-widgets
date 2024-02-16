package com.glia.widgets.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.UiTheme
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.asStateFlowable
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.withRuntimeTheme
import com.glia.widgets.view.dialog.holder.DialogHolderActivity
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

internal open class BaseActivityStackWatcher : SimpleActivityLifecycleCallbacks() {

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
    private val resumedActivities: List<Activity> get() = _resumedActivities.getAll()

    private val resumedActivitySubject: PublishSubject<List<Activity>> = PublishSubject.create()
    val topActivityObserver: Observable<Activity> = resumedActivitySubject.filter { list -> list.isNotEmpty() }.map { list -> list.last() }

    override fun onActivityResumed(activity: Activity) {
        _resumedActivities.add(activity)
        resumedActivitySubject.onNext(resumedActivities)
    }

    override fun onActivityPaused(activity: Activity) {
        _resumedActivities.remove(activity)
        resumedActivitySubject.onNext(resumedActivities)
    }
}

/**
 * This class will help in cases when only the resumed activity is needed instead of the top activity of the stack.
 *
 * Example: When the user navigates to the `Notifications` settings screen, the resumed activity will be null,
 * so we can properly handle the return from that screen.
 */
internal open class BaseSingleActivityWatcher(private val gliaActivityManager: GliaActivityManager) : SimpleActivityLifecycleCallbacks() {
    private val _resumedActivity: PublishProcessor<WeakReference<Activity>> = PublishProcessor.create()
    val resumedActivity: Flowable<WeakReference<Activity>> = _resumedActivity.asStateFlowable()

    private var alertDialog: AlertDialog? = null

    /* @CallSuper is added in case someone wants to open this function and override it */
    @CallSuper
    final override fun onActivityResumed(activity: Activity) {
        _resumedActivity.onNext(WeakReference(activity))
    }

    @CallSuper
    final override fun onActivityPaused(activity: Activity) {
        _resumedActivity.onNext(WeakReference(null))
    }

    @CallSuper
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        gliaActivityManager.onActivityCreated(activity)
    }

    @CallSuper
    final override fun onActivityDestroyed(activity: Activity) {
        gliaActivityManager.onActivityDestroyed(activity)
    }

    fun finishActivities() = gliaActivityManager.finishActivities()

    fun finishActivity(kClass: KClass<out Activity>) = gliaActivityManager.finishActivity(kClass)


    protected fun launchDialogHolderActivity(activity: Activity) {
        DialogHolderActivity.start(activity)
    }

    private fun enforceGliaActivity(activity: Activity, callback: Activity.() -> Unit) {
        if (activity.isGlia) {
            callback(activity)
        } else {
            launchDialogHolderActivity(activity)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun showAlertDialogWithStyledContext(activity: Activity, callback: (Context, UiTheme) -> AlertDialog) {
        enforceGliaActivity(activity) {
            dismissAlertDialogSilently()
            withRuntimeTheme { themedContext, uiTheme ->
                alertDialog = callback(themedContext, uiTheme)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun dismissAlertDialogSilently() {
        alertDialog?.dismiss()
        alertDialog = null
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
