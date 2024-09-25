package com.glia.widgets.operator

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.ArrayMap
import androidx.core.content.getSystemService
import com.glia.androidsdk.Engagement
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.call.CallActivity
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.showToast
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.view.Dialogs
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference
import com.glia.widgets.operator.OperatorRequestContract.State as ControllerState

private const val TAG = "RequestHandlerActivityWatcher"

internal class OperatorRequestActivityWatcher(
    private val controller: OperatorRequestContract.Controller,
    private val activityLauncher: ActivityLauncher,
    gliaActivityManager: GliaActivityManager
) : BaseSingleActivityWatcher(gliaActivityManager) {

    private val mediaProjectionResultLaunchers: ArrayMap<String, ActivityResultLauncher<Intent>> = ArrayMap()

    init {
        Flowable.combineLatest(resumedActivity, controller.state, ::handleState).subscribe()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)

        if (activity !is ComponentActivity) return

        registerForMediaProjectionPermissionResult(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        super.onActivityDestroyed(activity)
        mediaProjectionResultLaunchers.remove(activity.localClassName)
    }

    private fun handleState(activityReference: WeakReference<Activity>, event: OneTimeEvent<ControllerState>) {
        val state = event.value
        val activity = activityReference.get()

        when {
            event.consumed -> Logger.d(TAG, "skipping.., event is already consumed")
            activity == null || activity.isFinishing -> Logger.d(TAG, "skipping.. activity is null or finishing")
            state is ControllerState.DismissAlertDialog -> event.consume { dismissAlertDialogSilently() }
            state is ControllerState.OpenCallActivity -> event.consume { openCallActivity(state.mediaType, activity) }
            state is ControllerState.RequestMediaUpgrade -> showUpgradeDialog(state, activity, event::markConsumed)
            state is ControllerState.ShowScreenSharingDialog -> showScreenSharingDialog(state.operatorName, activity, event::markConsumed)
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
            mediaProjectionResultLaunchers[activity.localClassName]?.launch(
                activity.getSystemService<MediaProjectionManager>()?.createScreenCaptureIntent()
            )
        }
    }

    private fun openOverlayPermissionsScreen(activity: Activity) {
        activityLauncher.launchOverlayPermission(
            context = activity,
            onSuccess = {
                controller.overlayPermissionScreenOpened()
            }, onFailure = {
                controller.failedToOpenOverlayPermissionScreen()
            })
    }

    private fun enforceComponentActivity(activity: Activity, callback: () -> Unit) {
        if (activity is ComponentActivity) {
            callback()
        } else {
            launchDialogHolderActivity(activity)
        }
    }

    private fun registerForMediaProjectionPermissionResult(activity: ComponentActivity) {
        mediaProjectionResultLaunchers[activity.localClassName] =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                controller.onMediaProjectionResultReceived(it, activity)
            }
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

    private fun openCallActivity(mediaType: Engagement.MediaType, activity: Activity) {
        when {
            activity is CallActivity -> return
            activity.isGlia -> finishActivities()
        }

        activityLauncher.launchCall(activity, mediaType)
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
