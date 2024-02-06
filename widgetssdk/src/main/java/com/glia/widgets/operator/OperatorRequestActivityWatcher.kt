package com.glia.widgets.operator

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.IntentConfigurationHelper
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isGlia
import com.glia.widgets.view.Dialogs
import io.reactivex.Flowable
import java.lang.ref.WeakReference
import com.glia.widgets.operator.OperatorRequestContract.State as ControllerState

private const val TAG = "RequestHandlerActivityWatcher"

internal class OperatorRequestActivityWatcher(
    private val controller: OperatorRequestContract.Controller,
    private val intentConfigurationHelper: IntentConfigurationHelper,
    gliaActivityManager: GliaActivityManager
) : BaseSingleActivityWatcher(gliaActivityManager) {

    init {
        Flowable.combineLatest(resumedActivity, controller.state, ::handleState).subscribe()
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<ControllerState>) {
        val state = event.value
        val activity = activityReference.get() ?: return

        when {
            event.consumed || activity.isFinishing -> Logger.d(TAG, "skipping state: $state activity: $activity")
            state is ControllerState.DismissAlertDialog -> event.consume { dismissAlertDialogSilently() }
            state is ControllerState.OpenCallActivity -> event.consume { openCallActivity(state.mediaType, activity) }
            state is ControllerState.RequestMediaUpgrade -> showUpgradeDialog(state, activity, event::markConsumed)
        }
    }

    private fun openCallActivity(mediaType: Engagement.MediaType, activity: Activity) {
        when {
            activity is CallActivity -> return
            activity.isGlia -> finishActivities()
        }

        activity.startActivity(intentConfigurationHelper.createForCall(activity, mediaType))
    }

    private fun showUpgradeDialog(state: ControllerState.RequestMediaUpgrade, activity: Activity, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, theme ->
            Dialogs.showUpgradeDialog(context, theme, state.data, {
                consumeCallback()
                controller.onMediaUpgradeAccepted(state.data.offer, activity)
            }) {
                consumeCallback()
                controller.onMediaUpgradeDeclined(state.data.offer, activity)
            }
        }
    }

}
