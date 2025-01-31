package com.glia.widgets.engagement.completion

import android.app.Activity
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.view.Dialogs
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference

internal class EngagementCompletionActivityWatcher(
    controller: EngagementCompletionContract.Controller,
    gliaActivityManager: GliaActivityManager,
    private val activityLauncher: ActivityLauncher
) : BaseSingleActivityWatcher(gliaActivityManager) {

    init {
        Flowable.combineLatest(resumedActivity, controller.state, ::handleState).subscribe()
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<EngagementCompletionState>) {
        val state = event.value
        val activity = activityReference.get()

        when {
            event.consumed -> Logger.d(TAG, "skipping.. event: ${event.value} is already consumed")
            /*
            * This state finishes all SDK activities even if the application is in the background(activity == null),
            * to be sure that everything related to the queueing or engagement is released.
            * */
            state is EngagementCompletionState.FinishActivities -> event.consume { finishActivities() }
            activity == null || activity.isFinishing -> Logger.d(TAG, "skipping.. activity is null or finishing")
            state is EngagementCompletionState.ShowNoOperatorsAvailableDialog -> showNoOperatorsAvailableDialog(activity, event::markConsumed)
            state is EngagementCompletionState.ShowUnexpectedErrorDialog -> showUnexpectedErrorDialog(activity, event::markConsumed)
            state is EngagementCompletionState.ShowEngagementEndedDialog -> showOperatorEndedEngagementDialog(activity, event::markConsumed)
            state is EngagementCompletionState.ShowSurvey -> event.consume { showSurvey(activity, state.survey) }
        }
    }

    private fun showSurvey(activity: Activity, survey: Survey) = activityLauncher.launchSurvey(activity, survey)

    private fun showOperatorEndedEngagementDialog(activity: Activity, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, theme ->
            Dialogs.showOperatorEndedEngagementDialog(context = context, theme = theme) {
                dismissAlertDialogSilently()
                finishActivities()
                consumeCallback()
            }
        }
    }

    private fun showNoOperatorsAvailableDialog(activity: Activity, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, theme ->
            Dialogs.showNoMoreOperatorsAvailableDialog(context = context, uiTheme = theme) {
                dismissAlertDialogSilently()
                finishActivities()
                consumeCallback()
            }
        }
    }

    private fun showUnexpectedErrorDialog(activity: Activity, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, theme ->
            Dialogs.showUnexpectedErrorDialog(context = context, uiTheme = theme) {
                dismissAlertDialogSilently()
                finishActivities()
                consumeCallback()
            }
        }
    }
}
