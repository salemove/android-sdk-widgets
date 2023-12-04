package com.glia.widgets.engagement.end

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.base.SimpleActivityLifecycleCallbacks
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.qualifiedName
import com.glia.widgets.survey.SurveyActivity
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.dialog.holder.DialogHolderActivity
import java.lang.ref.WeakReference

@SuppressLint("CheckResult")
internal class ActivityWatcherForEngagementEnd @JvmOverloads constructor(
    private val controller: EngagementCompletionController,
    private val activities: ArrayMap<String, WeakReference<Activity>> = ArrayMap()
) : SimpleActivityLifecycleCallbacks() {
    private var alertDialog: AlertDialog? = null

    init {
        controller.state.subscribe {
            when (it) {
                is EngagementCompletionController.State.LaunchDialogHolderActivity -> launchDialogHolderActivity(it.activity)
                EngagementCompletionController.State.ReleaseControllersAndUi -> releaseResources()
                is EngagementCompletionController.State.ShowOperatorEndedEngagementDialog -> showDialog(it.themedContext, it.uiTheme)
                is EngagementCompletionController.State.ShowSurvey -> showSurvey(it.activity, it.survey, it.uiTheme)
                EngagementCompletionController.State.Skip -> Logger.d(TAG, "New Activity is attached. Skipping event...")
            }
        }
    }

    private fun launchDialogHolderActivity(activity: Activity) {
        DialogHolderActivity.start(activity)
    }

    private fun showSurvey(activity: Activity, survey: Survey, theme: UiTheme) {
        activity.apply {
            val newIntent: Intent = Intent(this, SurveyActivity::class.java)
                .putExtra(GliaWidgets.UI_THEME, theme)
                .putExtra(GliaWidgets.SURVEY, survey as Parcelable)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(newIntent)
        }
    }

    private fun showDialog(context: Context, theme: UiTheme) {
        alertDialog = Dialogs.showOperatorEndedEngagementDialog(context = context, theme = theme) {
            dismissDialog()
            finishActivities()
        }
    }

    private fun insertActivity(activity: Activity) {
        if (activity.isGlia) {
            activities[activity.qualifiedName] = WeakReference(activity)
        }
    }

    private fun removeActivity(activity: Activity) {
        activities.remove(activity.qualifiedName)
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        insertActivity(activity)
        controller.captureTheme(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        controller.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        controller.onActivityPaused()
    }

    private fun releaseResources() {
        Dependencies.destroyControllers()
        finishActivities()
    }

    private fun finishActivities() {
        activities.values.mapNotNull(WeakReference<Activity>::get).forEach(Activity::finish)
        activities.clear()
    }

    private fun dismissDialog() {
        alertDialog?.dismiss()
    }
}
