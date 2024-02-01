package com.glia.widgets.helper

import android.app.Activity
import androidx.collection.ArrayMap
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

internal class GliaActivityManager {
    private val activities: ArrayMap<String, WeakReference<Activity>> = ArrayMap()

    private fun insertActivity(activity: Activity) {
        if (activity.isGlia) {
            activities[activity.qualifiedName] = WeakReference(activity)
        }
    }

    private fun removeActivity(activity: Activity) {
        activities.remove(activity.qualifiedName)
    }

    fun onActivityCreated(activity: Activity) {
        insertActivity(activity)
    }

    fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity)
    }

    fun finishActivities() {
        activities.values.mapNotNull(WeakReference<Activity>::get).forEach(Activity::finish)
        activities.clear()
    }

    fun finishActivity(kClass: KClass<out Activity>) {
        activities.remove(kClass.qualifiedName)?.get()?.finish()
    }
}
