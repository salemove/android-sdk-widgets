package com.glia.widgets.engagement.completion

import android.app.Activity
import com.glia.androidsdk.Engagement
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
            activity == null || activity.isFinishing -> {
                Logger.d(TAG, "skipping.. activity is null or finishing")
                if (state is EngagementCompletionState.EngagementEnded) event.consume { finishActivities() }
            }
            state is EngagementCompletionState.QueueUnstaffed -> showQueueUnstaffedDialog(activity, event::markConsumed)
            state is EngagementCompletionState.UnexpectedErrorHappened -> showUnexpectedErrorDialog(activity, event::markConsumed)
            state is EngagementCompletionState.EngagementEnded -> state.handleEngagementEndedEvent(activity, event::markConsumed)
            state is EngagementCompletionState.SurveyLoaded -> event.consume { showSurvey(activity, state.survey) }
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

    private fun showQueueUnstaffedDialog(activity: Activity, consumeCallback: () -> Unit) {
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

    private fun EngagementCompletionState.EngagementEnded.handleEngagementEndedEvent(activity: Activity, onEventHandled: () -> Unit) {
        if (actionOnEnd == Engagement.ActionOnEnd.UNKNOWN) {
            Logger.w(TAG, "Engagement ended with unknown case.")
        }
        if (isEndedByVisitor || actionOnEnd == Engagement.ActionOnEnd.RETAIN || actionOnEnd == Engagement.ActionOnEnd.SHOW_SURVEY) {
            onEventHandled()
        } else if (actionOnEnd == Engagement.ActionOnEnd.END_NOTIFICATION || actionOnEnd == Engagement.ActionOnEnd.UNKNOWN) {
            showOperatorEndedEngagementDialog(activity, onEventHandled)
        }
    }
}
