package com.glia.widgets.core.screensharing

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase
import com.glia.widgets.core.screensharing.data.GliaScreenSharingRepository
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal class ScreenSharingControllerImpl(
    private val repository: GliaScreenSharingRepository,
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

    override fun init() {
        repository.init(this)
    }

    override fun onScreenSharingRequest() {
        Logger.d(TAG, "on screen sharing request")
        if (viewCallbacks.isNotEmpty()) {
            if (!hasScreenSharingNotificationChannelEnabledUseCase.invoke()) {
                hasPendingScreenSharingRequest = true
                dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog()
            } else {
                dialogController.showStartScreenSharingDialog()
            }
        }
    }

    override fun onScreenSharingStarted() {
        Logger.d(TAG, "screen sharing started")
        viewCallbacks.forEach { it.onScreenSharingStarted() }
    }

    override fun onScreenSharingEnded() {
        Logger.d(TAG, "screen sharing ended")
        viewCallbacks.forEach { it.onScreenSharingEnded() }
    }

    override fun onScreenSharingRequestError(exception: GliaException) {
        Logger.e(TAG, "screen sharing request error: " + exception.message)
        viewCallbacks.forEach { it.onScreenSharingRequestError(exception) }
        hideScreenSharingEnabledNotification()
    }

    override fun onScreenSharingRequestSuccess() {
        Logger.d(TAG, "screen sharing request success")
        viewCallbacks.forEach { it.onScreenSharingRequestSuccess() }
    }

    override fun onResume(activity: Activity, requestScreenSharingCallback: (() -> Unit)?) {
        // spam all the time otherwise no way to end screen sharing
        if (hasPendingScreenSharingRequest) {
            if (!hasScreenSharingNotificationChannelEnabledUseCase.invoke()) {
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

    fun onDestroy() {
        Logger.d(TAG, "onDestroy")
        repository.onDestroy()
    }

    override val isSharingScreen: Boolean
        get() = repository.isSharingScreen

    override fun onScreenSharingAccepted(activity: Activity) {
        Logger.d(TAG, "onScreenSharingAccepted")
        dialogController.dismissCurrentDialog()
        showScreenSharingEnabledNotification()
        repository.onScreenSharingAccepted(
            activity,
            gliaSdkConfigurationManager.screenSharingMode
        )
        hasPendingScreenSharingRequest = false
    }

    override fun onScreenSharingAcceptedAndPermissionAsked(activity: Activity) {
        Logger.d(TAG, "onScreenSharingAcceptedAndPermissionAsked")
        dialogController.dismissCurrentDialog()
        showScreenSharingEnabledNotification()
        repository.onScreenSharingAcceptedAndPermissionAsked(
            activity,
            gliaSdkConfigurationManager.screenSharingMode
        )
        hasPendingScreenSharingRequest = false
    }

    override fun onScreenSharingDeclined() {
        Logger.d(TAG, "onScreenSharingDeclined")
        dialogController.dismissCurrentDialog()
        repository.onScreenSharingDeclined()
        hasPendingScreenSharingRequest = false
        hideScreenSharingEnabledNotification()
    }

    override fun onScreenSharingNotificationEndPressed() {
        hideScreenSharingEnabledNotification()
        repository.onEndScreenSharing()
    }

    override fun setViewCallback(callback: ScreenSharingController.ViewCallback?) {
        callback?.run { viewCallbacks.add(this) }
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
        repository.forceEndScreenSharing()
        removeScreenSharingNotificationUseCase()
    }
}
