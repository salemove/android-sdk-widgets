package com.glia.widgets.view.dialog

import android.app.Activity
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.snackbar.SnackBarDelegateFactory
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference

internal class UiComponentsActivityWatcher(
    gliaActivityManager: GliaActivityManager,
    uiComponentsDispatcher: UiComponentsDispatcher,
    private val localeProvider: LocaleProvider,
    private val themeManager: UnifiedThemeManager,
    private val activityLauncher: ActivityLauncher
) : BaseSingleActivityWatcher(gliaActivityManager) {

    init {
        Flowable.combineLatest(resumedActivity, uiComponentsDispatcher.state, ::handleState).subscribe()
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<UiComponentsDispatcher.State>) {
        val activity = activityReference.get()
        val state = event.value

        when {
            event.consumed -> Logger.d(TAG, "skipping.., event is already consumed")

            activity == null -> Logger.d(TAG, "skipping.. activity is null")

            state is UiComponentsDispatcher.State.DismissDialog -> event.consume { dismissDialogAndFinishHolderActivity() }

            activity.isFinishing -> Logger.d(TAG, "skipping.. activity is finishing")

            state is UiComponentsDispatcher.State.NotificationPermissionDialog -> showPermissionsDialog(
                activity,
                event::markConsumed,
                state.onAllow,
                state.onCancel
            )

            state is UiComponentsDispatcher.State.ShowSnackBar -> event.consume {
                showSnackbar(activity, state.messageResId)
            }

            state is UiComponentsDispatcher.State.LaunchChatScreen -> event.consume {
                activityLauncher.launchChat(activity, intention = state.intention)
            }
        }
    }

    private fun showSnackbar(activity: Activity, messageResId: Int) =
        SnackBarDelegateFactory(
            activity,
            messageResId,
            localeProvider,
            themeManager.theme
        ).createDelegate().show()

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
