package com.glia.widgets.core.screensharing

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.unSafeSubscribe

internal class ScreenSharingControllerImpl(
    private val screenSharingUseCase: ScreenSharingUseCase,
    private val dialogController: DialogController,
    private val showScreenSharingNotificationUseCase: ShowScreenSharingNotificationUseCase,
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val hasScreenSharingNotificationChannelEnabledUseCase: HasScreenSharingNotificationChannelEnabledUseCase,
    private val gliaSdkConfigurationManager: GliaSdkConfigurationManager
) : ScreenSharingController {
    @VisibleForTesting
    val viewCallbacks: MutableSet<ScreenSharingController.ViewCallback> = HashSet()

    @JvmField
    @VisibleForTesting
    var hasPendingScreenSharingRequest = false

    init {
        screenSharingUseCase().unSafeSubscribe {
            when (it) {
                ScreenSharingState.Ended -> onScreenSharingEnded()
                is ScreenSharingState.FailedToAcceptRequest -> onScreenSharingRequestError(it.message)
                ScreenSharingState.RequestAccepted -> onScreenSharingRequestSuccess()
                ScreenSharingState.Requested -> onScreenSharingRequest()
                ScreenSharingState.Started -> onScreenSharingStarted()
                ScreenSharingState.RequestDeclined -> Logger.d(TAG, "Screen sharing request declined")
            }
        }
    }

    fun onScreenSharingRequest() {
        if (viewCallbacks.isNotEmpty()) {
            if (!hasScreenSharingNotificationChannelEnabledUseCase()) {
                hasPendingScreenSharingRequest = true
                dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog()
            } else {
                dialogController.showStartScreenSharingDialog()
            }
        }
    }

    private fun onScreenSharingStarted() {
        viewCallbacks.forEach { it.onScreenSharingStarted() }
    }

    private fun onScreenSharingEnded() {
        viewCallbacks.forEach { it.onScreenSharingEnded() }
    }

    fun onScreenSharingRequestError(message: String) {
        viewCallbacks.forEach { it.onScreenSharingRequestError(message) }
        hideScreenSharingEnabledNotification()
    }

    private fun onScreenSharingRequestSuccess() {
        viewCallbacks.forEach { it.onScreenSharingRequestSuccess() }
    }

    override fun onResume(activity: Activity, requestScreenSharingCallback: (() -> Unit)?) {
        // spam all the time otherwise no way to end screen sharing
        if (hasPendingScreenSharingRequest) {
            if (!hasScreenSharingNotificationChannelEnabledUseCase()) {
                if (dialogController.isEnableScreenSharingNotificationsAndStartSharingDialogShown) {
                    // Do not need to request dialog again if this dialog is already shown.
                    //
                    // It prevents the infinity cycle of trying to display
                    // this dialog if the CallVisualizerSupportActivity is used for it.
                    return
                }
                dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog()
            } else {
                if (requestScreenSharingCallback != null) {
                    requestScreenSharingCallback()
                } else {
                    onScreenSharingAccepted(activity)
                }
            }
        }
    }

    override val isSharingScreen: Boolean
        get() = screenSharingUseCase.isSharing

    override fun onScreenSharingAccepted(activity: Activity) {
        Logger.d(TAG, "onScreenSharingAccepted")
        dialogController.dismissCurrentDialog()
        showScreenSharingEnabledNotification()
        screenSharingUseCase.acceptRequest(activity, gliaSdkConfigurationManager.screenSharingMode)
        hasPendingScreenSharingRequest = false
    }

    override fun onScreenSharingAcceptedAndPermissionAsked(activity: Activity) {
        Logger.d(TAG, "onScreenSharingAcceptedAndPermissionAsked")
        dialogController.dismissCurrentDialog()
        showScreenSharingEnabledNotification()
        screenSharingUseCase.acceptRequestWithAskedPermission(activity, gliaSdkConfigurationManager.screenSharingMode)
        hasPendingScreenSharingRequest = false
    }

    override fun onScreenSharingDeclined() {
        Logger.d(TAG, "onScreenSharingDeclined")
        dialogController.dismissCurrentDialog()
        screenSharingUseCase.declineRequest()
        hasPendingScreenSharingRequest = false
        hideScreenSharingEnabledNotification()
    }

    override fun onScreenSharingNotificationEndPressed() {
        hideScreenSharingEnabledNotification()
        screenSharingUseCase.end()
    }

    override fun onActivityResultSkipPermissionRequest(resultCode: Int, data: Intent?) {
        screenSharingUseCase.onActivityResultSkipPermissionRequest(resultCode, data)
    }

    override fun setViewCallback(callback: ScreenSharingController.ViewCallback?) {
        callback?.run(viewCallbacks::add)
    }

    override fun removeViewCallback(callback: ScreenSharingController.ViewCallback?) {
        viewCallbacks.remove(callback)
    }

    private fun showScreenSharingEnabledNotification() {
        showScreenSharingNotificationUseCase()
    }

    override fun hideScreenSharingEnabledNotification() {
        removeScreenSharingNotificationUseCase()
    }

    override fun onForceStopScreenSharing() {
        screenSharingUseCase.end()
        removeScreenSharingNotificationUseCase()
    }
}
