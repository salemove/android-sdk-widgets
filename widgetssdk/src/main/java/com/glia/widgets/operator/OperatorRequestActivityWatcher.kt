package com.glia.widgets.operator

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import com.glia.androidsdk.Engagement
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.core.notification.openNotificationChannelScreen
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.IntentConfigurationHelper
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.showToast
import com.glia.widgets.view.Dialogs
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference
import kotlin.properties.Delegates
import com.glia.widgets.operator.OperatorRequestContract.State as ControllerState

private const val TAG = "RequestHandlerActivityWatcher"

internal class OperatorRequestActivityWatcher(
    private val controller: OperatorRequestContract.Controller,
    private val intentConfigurationHelper: IntentConfigurationHelper,
    gliaActivityManager: GliaActivityManager
) : BaseSingleActivityWatcher(gliaActivityManager) {

    private var mediaProjectionResultLauncher: ActivityResultLauncher<Intent> by Delegates.notNull()

    init {
        Flowable.combineLatest(resumedActivity, controller.state, ::handleState).subscribe()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)

        if (activity !is ComponentActivity) return

        registerForMediaProjectionPermissionResult(activity)
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<ControllerState>) {
        val state = event.value
        val activity = activityReference.get()

        when {
            event.consumed -> Logger.d(TAG, "skipping.., event is already consumed")
            activity == null && state is ControllerState.WaitForNotificationScreenOpen -> event.consume { controller.onNotificationScreenOpened() }
            activity == null || activity.isFinishing -> Logger.d(TAG, "skipping.. activity is null or finishing")
            state is ControllerState.DismissAlertDialog -> event.consume { dismissAlertDialogSilently() }
            state is ControllerState.OpenCallActivity -> event.consume { openCallActivity(state.mediaType, activity) }
            state is ControllerState.RequestMediaUpgrade -> showUpgradeDialog(state, activity, event::markConsumed)
            state is ControllerState.EnableScreenSharingNotificationsAndStartSharing -> showEnableScreenSharingNotifications(
                activity, event::markConsumed
            )

            state is ControllerState.ShowScreenSharingDialog -> showScreenSharingDialog(state.operatorName, activity, event::markConsumed)
            state is ControllerState.OpenNotificationsScreen -> event.consume { openNotificationsScreen(activity) }
            state is ControllerState.WaitForNotificationScreenResult -> event.consume { controller.onReturnedFromNotificationScreen() }
            state is ControllerState.AcquireMediaProjectionToken -> requestMediaProjection(activity, event::markConsumed)
            state is ControllerState.DisplayToast -> event.consume { displayToast(activity, state.message) }
            state is ControllerState.ShowOverlayDialog -> showOverlayDialog(activity, event::markConsumed)
            state is ControllerState.OpenOverlayPermissionScreen -> event.consume { openOverlayPermissionsScreen(activity) }
        }
    }

    private fun displayToast(activity: Activity, message: String) {
        activity.showToast(message)
    }

    private fun requestMediaProjection(activity: Activity, consumeCallback: () -> Unit) {
        enforceComponentActivity(activity) {
            consumeCallback()
            mediaProjectionResultLauncher.launch(activity.getSystemService<MediaProjectionManager>()?.createScreenCaptureIntent())
        }
    }

    private fun openOverlayPermissionsScreen(activity: Activity) {
        val overlayIntent = intentConfigurationHelper.createForOverlayPermissionScreen(activity)

        if (overlayIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(overlayIntent)
            controller.overlayPermissionScreenOpened()
        } else {
            controller.failedToOpenOverlayPermissionScreen()
        }
    }

    private fun enforceComponentActivity(activity: Activity, callback: () -> Unit) {
        if (activity is ComponentActivity) {
            callback()
        } else {
            launchDialogHolderActivity(activity)
        }
    }

    private fun registerForMediaProjectionPermissionResult(activity: ComponentActivity) {
        mediaProjectionResultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            controller.onMediaProjectionResultReceived(it, activity)
        }
    }

    private fun openNotificationsScreen(activity: Activity) {
        activity.openNotificationChannelScreen()
        controller.onNotificationScreenRequested()
    }

    private fun showOverlayDialog(activity: Activity, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, uiTheme ->
            Dialogs.showOverlayPermissionsDialog(context, uiTheme, {
                consumeCallback()
                controller.onOverlayPermissionRequestAccepted(activity)
            }) {
                consumeCallback()
                controller.onOverlayPermissionRequestDeclined(activity)
            }
        }
    }

    private fun showScreenSharingDialog(operatorName: String?, activity: Activity, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, uiTheme ->
            Dialogs.showScreenSharingDialog(context, uiTheme, operatorName, {
                consumeCallback()
                controller.onScreenSharingDialogAccepted(activity)
            }) {
                consumeCallback()
                controller.onScreenSharingDialogDeclined(activity)
            }
        }
    }

    private fun showEnableScreenSharingNotifications(activity: Activity, consumeCallback: () -> Unit) {
        showAlertDialogWithStyledContext(activity) { context, uiTheme ->
            Dialogs.showAllowScreenSharingNotificationsAndStartSharingDialog(context, uiTheme, {
                consumeCallback()
                controller.onShowEnableScreenSharingNotificationsAccepted()
            }) {
                consumeCallback()
                controller.onShowEnableScreenSharingNotificationsDeclined(activity)
            }
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
