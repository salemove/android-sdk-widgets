package com.glia.widgets.engagement.completion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.base.SimpleActivityLifecycleCallbacks
import com.glia.widgets.engagement.completion.EngagementCompletionContract.State
import com.glia.widgets.helper.GliaTransparentActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.qualifiedName
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.survey.SurveyActivity
import com.glia.widgets.view.Dialogs
import java.lang.ref.WeakReference

internal class EngagementCompletionActivityWatcher @JvmOverloads constructor(
    private val controller: EngagementCompletionContract.Controller,
    private val activities: ArrayMap<String, WeakReference<Activity>> = ArrayMap()
) : SimpleActivityLifecycleCallbacks() {
    private var alertDialog: AlertDialog? = null

    init {
        controller.state.unSafeSubscribe {
            when (it) {
                is State.LaunchDialogHolderActivity -> launchDialogHolderActivity(it.activity)
                State.ReleaseUi -> finishActivities()
                is State.ShowOperatorEndedEngagementDialog -> showOperatorEndedEngagementDialog(it.themedContext, it.uiTheme, it.onHandledCallback)
                is State.ShowQueueUnstaffedDialog -> showQueueUnstaffedDialog(it.themedContext, it.uiTheme, it.onHandledCallback)
                is State.ShowUnexpectedDialog -> showUnexpectedErrorDialog(it.themedContext, it.uiTheme, it.onHandledCallback)
                is State.ShowSurvey -> showSurvey(it.activity, it.survey, it.uiTheme)
                State.Ignore -> Logger.d(TAG, "New Activity is attached. Skipping event...")
            }
        }
    }

    private fun launchDialogHolderActivity(activity: Activity) {
        GliaTransparentActivity.start(activity)
    }

    private fun showSurvey(activity: Activity, survey: Survey, theme: UiTheme) {
        activity.apply {
            val newIntent: Intent = Intent(this, SurveyActivity::class.java)
                .putExtra(GliaWidgets.UI_THEME, theme)
                .putExtra(GliaWidgets.SURVEY, survey as Parcelable)

            overridePendingTransition(R.anim.slide_up, 0)
            startActivity(newIntent)
        }
    }

    private fun showOperatorEndedEngagementDialog(context: Context, theme: UiTheme, onHandledCallback: () -> Unit) {
        alertDialog = Dialogs.showOperatorEndedEngagementDialog(context = context, theme = theme) {
            dismissDialog()
            finishActivities()
            onHandledCallback()
        }
    }

    private fun showQueueUnstaffedDialog(context: Context, theme: UiTheme, onHandledCallback: () -> Unit) {
        alertDialog = Dialogs.showNoMoreOperatorsAvailableDialog(context = context, uiTheme = theme) {
            dismissDialog()
            finishActivities()
            onHandledCallback()
        }
    }

    private fun showUnexpectedErrorDialog(context: Context, theme: UiTheme, onHandledCallback: () -> Unit) {
        alertDialog = Dialogs.showUnexpectedErrorDialog(context = context, uiTheme = theme) {
            dismissDialog()
            finishActivities()
            onHandledCallback()
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

    private fun finishActivities() {
        activities.values.mapNotNull(WeakReference<Activity>::get).forEach(Activity::finish)
        activities.clear()
    }

    private fun dismissDialog() {
        alertDialog?.dismiss()
    }
}
