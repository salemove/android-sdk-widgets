package com.glia.widgets.callvisualizer

import android.app.Activity
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.Dialogs
import com.glia.widgets.webbrowser.WebBrowserActivity
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract.State as ControllerState

internal class CallVisualizerActivityWatcher(
    private val controller: CallVisualizerContract.Controller,
    gliaActivityManager: GliaActivityManager
) : BaseSingleActivityWatcher(gliaActivityManager) {

    init {
        Flowable.combineLatest(resumedActivity, controller.state, ::handleState).subscribe()
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<CallVisualizerContract.State>) {
        val state = event.value
        val activity = activityReference.get()

        when {
            event.consumed -> Logger.d(TAG, "skipping.., event is already consumed")
            activity == null || activity.isFinishing -> Logger.d(TAG, "skipping.. activity is null or finishing")
            activity is WebBrowserActivity && state is ControllerState.DisplayConfirmationDialog -> Logger.d(TAG, "skipping.. WebBrowser is open")
            activity is WebBrowserActivity && state is ControllerState.OpenWebBrowserScreen -> event.consume { controller.onWebBrowserOpened() }
            state is ControllerState.DismissDialog -> event.consume { dismissAlertDialogSilently() }
            //Ensure this state remains unconsumed until the opening of the WebBrowserActivity.
            state is ControllerState.OpenWebBrowserScreen -> openWebBrowser(activity, state.title, state.url)
            state is ControllerState.CloseHolderActivity -> event.consume { closeHolderActivity() }
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
        val intent = WebBrowserActivity.intent(activity, title, url)
        activity.startActivity(intent)
    }
}
