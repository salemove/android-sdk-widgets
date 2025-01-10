package com.glia.widgets.callvisualizer

import android.app.Activity
import androidx.annotation.StringRes
import com.glia.widgets.R
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.snackbar.SnackBarDelegateFactory
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager
import com.glia.widgets.webbrowser.WebBrowserActivity
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract.State as ControllerState

internal class CallVisualizerActivityWatcher(
    private val controller: CallVisualizerContract.Controller,
    gliaActivityManager: GliaActivityManager,
    private val localeProvider: LocaleProvider,
    private val themeManager: UnifiedThemeManager,
    private val activityLauncher: ActivityLauncher
) : BaseSingleActivityWatcher(gliaActivityManager) {

    init {
        Flowable.combineLatest(resumedActivity, controller.state, ::handleState).subscribe()
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<CallVisualizerContract.State>) {
        val state = event.value
        val activity = activityReference.get()

        when {
            event.consumed -> Logger.d(TAG, "skipping.., event is already consumed")
            state is ControllerState.DismissDialog -> event.consume { dismissAlertDialogSilently() }
            state is ControllerState.CloseHolderActivity -> event.consume { closeHolderActivity() }
            activity == null || activity.isFinishing -> Logger.d(TAG, "skipping.. activity is null or finishing")
            activity is WebBrowserActivity && state is ControllerState.DisplayConfirmationDialog -> Logger.d(TAG, "skipping.. WebBrowser is open")
            activity is WebBrowserActivity && state is ControllerState.OpenWebBrowserScreen -> event.consume { controller.onWebBrowserOpened() }
            state is ControllerState.ShowTimeoutSnackBar -> event.consume { showTimedOutSnackBar(activity) }
            state is ControllerState.ShowAlreadyInCvSnackBar -> event.consume { showAlreadyInCvSnackBar(activity) }
            //Ensure this state remains unconsumed until the opening of the WebBrowserActivity.
            state is ControllerState.OpenWebBrowserScreen -> openWebBrowser(activity, state.title, state.url)
            state is ControllerState.DisplayVisitorCodeDialog -> displayVisitorCodeDialog(activity)
            state is ControllerState.DisplayConfirmationDialog -> displayConfirmationDialog(
                activity,
                state.links,
                event::markConsumed
            )
        }

    }

    private fun closeHolderActivity() = finishActivity(DialogHolderActivity::class)

    private fun displayVisitorCodeDialog(activity: Activity) {
        showAlertDialogWithStyledContext(activity) { context, _ ->
            Dialogs.showVisitorCodeDialog(context)
        }
    }

    private fun displayConfirmationDialog(activity: Activity, links: ConfirmationDialogLinks, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, uiTheme ->
            Dialogs.showEngagementConfirmationDialog(
                context = context,
                theme = uiTheme,
                links = links,
                linkClickListener = {
                    consumeCallback()
                    controller.onLinkClicked(it)
                    dismissAlertDialogSilently()
                },
                positiveButtonClickListener = {
                    consumeCallback()
                    controller.onEngagementConfirmationDialogAllowed()
                    dismissAlertDialogSilently()
                },
                negativeButtonClickListener = {
                    consumeCallback()
                    controller.onEngagementConfirmationDialogDeclined()
                    dismissAlertDialogSilently()
                })
        }
    }

    private fun openWebBrowser(activity: Activity, title: LocaleString, url: String) {
        activityLauncher.launchWebBrowser(activity, title, url)
    }

    private fun showTimedOutSnackBar(activity: Activity) = showSnackBar(activity, R.string.engagement_incoming_request_timed_out_message)

    private fun showAlreadyInCvSnackBar(activity: Activity) = showSnackBar(activity, R.string.entry_widget_call_visualizer_description)

    private fun showSnackBar(activity: Activity, @StringRes messageRes: Int) = SnackBarDelegateFactory(
        activity,
        messageRes,
        localeProvider,
        themeManager.theme
    ).createDelegate().show()

}
