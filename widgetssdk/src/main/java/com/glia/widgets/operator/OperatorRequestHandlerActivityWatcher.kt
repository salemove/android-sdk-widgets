package com.glia.widgets.operator

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.glia.androidsdk.Engagement
import com.glia.widgets.UiTheme
import com.glia.widgets.base.BaseActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.IntentConfigurationHelper
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.runtimeTheme
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.dialog.holder.DialogHolderActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import com.glia.widgets.operator.OperatorRequestHandlerContract.State as ControllerState

private const val TAG = "RequestHandlerActivityWatcher"

internal class OperatorRequestHandlerActivityWatcher(
    private val controller: OperatorRequestHandlerContract.Controller,
    private val gliaActivityManager: GliaActivityManager,
    private val intentConfigurationHelper: IntentConfigurationHelper
) : BaseActivityWatcher() {
    private var alertDialog: AlertDialog? = null

    init {
        Flowable.combineLatest(topActivityObserver.toFlowable(BackpressureStrategy.LATEST), controller.state, ::handleState).subscribe()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        gliaActivityManager.onActivityCreated(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        gliaActivityManager.onActivityDestroyed(activity)
    }

    private fun handleState(activity: Activity, event: OneTimeEvent<ControllerState>) {
        val state = event.view()

        when {
            state == null || activity.isFinishing -> Logger.d(TAG, "skipping state: $state activity: $activity")
            state is ControllerState.DismissAlertDialog -> {
                event.markConsumed()
                dismissAlertDialogSilently()
            }

            state is ControllerState.OpenCallActivity -> {
                event.markConsumed()
                openCallActivity(state.mediaType, activity)
            }

            state is ControllerState.RequestMediaUpgrade -> showUpgradeDialog(state, activity, event::markConsumed)
        }
    }

    private fun openCallActivity(mediaType: Engagement.MediaType, activity: Activity) {
        when {
            activity is CallActivity -> return
            activity.isGlia -> gliaActivityManager.finishActivities()
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

    private fun launchDialogHolderActivity(activity: Activity) {
        DialogHolderActivity.start(activity)
    }

    private fun ensureGliaActivity(activity: Activity, callback: Activity.() -> Unit) {
        if (activity.isGlia) {
            callback(activity)
        } else {
            launchDialogHolderActivity(activity)
        }
    }

    private fun showAlertDialogWithStyledContext(activity: Activity, logMessage: String? = null, callback: (Context, UiTheme) -> AlertDialog) {
        ensureGliaActivity(activity) {
            runOnUiThread {
                logMessage?.let { Logger.d(TAG, it) }
                dismissAlertDialogSilently()
                alertDialog = callback(wrapWithMaterialThemeOverlay(), runtimeTheme)
            }
        }
    }

    private fun dismissAlertDialogSilently() {
        alertDialog?.dismiss()
        alertDialog = null
    }
}
