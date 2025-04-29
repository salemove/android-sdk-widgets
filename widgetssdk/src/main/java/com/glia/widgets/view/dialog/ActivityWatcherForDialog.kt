package com.glia.widgets.view.dialog

import android.app.Activity
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.view.Dialogs
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference

internal class ActivityWatcherForDialog(gliaActivityManager: GliaActivityManager, dialogDispatcher: DialogDispatcher) :
    BaseSingleActivityWatcher(gliaActivityManager) {

    init {
        Flowable.combineLatest(resumedActivity, dialogDispatcher.state, ::handleState).subscribe()
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<DialogDispatcher.State>) {
        val activity = activityReference.get()
        val state = event.value

        when {
            event.consumed -> Logger.d(TAG, "skipping.., event is already consumed")
            activity == null -> Logger.d(TAG, "skipping.. activity is null")
            state is DialogDispatcher.State.DismissDialog -> event.consume { dismissDialogAndFinishHolderActivity() }
            activity.isFinishing -> Logger.d(TAG, "skipping.. activity is finishing")
            state is DialogDispatcher.State.NotificationPermissionDialog -> showPermissionsDialog(
                activity,
                event::markConsumed,
                state.onAllow,
                state.onCancel
            )
        }
    }

    private fun showPermissionsDialog(activity: Activity, consumeCallback: () -> Unit, onAllow: () -> Unit, onCancel: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, uiTheme ->
            Dialogs.showPushNotificationsPermissionDialog(
                context = context,
                uiTheme = uiTheme,
                positiveButtonClickListener = {
                    dismissDialogAndFinishHolderActivity()
                    consumeCallback()
                    onAllow()
                },
                negativeButtonClickListener = {
                    dismissDialogAndFinishHolderActivity()
                    consumeCallback()
                    onCancel()
                })

        }
    }

    private fun dismissDialogAndFinishHolderActivity() {
        dismissAlertDialogSilently()
        finishActivity(DialogHolderActivity::class)
    }

}
