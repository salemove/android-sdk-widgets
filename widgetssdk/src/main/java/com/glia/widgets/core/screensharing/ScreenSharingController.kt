package com.glia.widgets.core.screensharing

import android.content.Context
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
import com.glia.widgets.helper.Utils
import java.util.function.Consumer

internal class ScreenSharingController(
    private val repository: GliaScreenSharingRepository,
    private val dialogController: DialogController,
    private val showScreenSharingNotificationUseCase: ShowScreenSharingNotificationUseCase,
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val hasScreenSharingNotificationChannelEnabledUseCase: HasScreenSharingNotificationChannelEnabledUseCase,
    private val gliaSdkConfigurationManager: GliaSdkConfigurationManager
) : GliaScreenSharingCallback {
    private val viewCallbacks: MutableSet<ViewCallback> = HashSet()

    @JvmField
    @VisibleForTesting
    var hasPendingScreenSharingRequest = false

    fun init() {
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
        viewCallbacks.forEach(Consumer { obj: ViewCallback -> obj.onScreenSharingStarted() })
    }

    override fun onScreenSharingEnded() {
        Logger.d(TAG, "screen sharing ended")
    }

    override fun onScreenSharingRequestError(exception: GliaException) {
        Logger.e(TAG, "screen sharing request error: " + exception.message)
        viewCallbacks.forEach(
            Consumer { callback: ViewCallback -> callback.onScreenSharingRequestError(exception) }
        )
        hideScreenSharingEnabledNotification()
    }

    override fun onScreenSharingRequestSuccess() {
        Logger.d(TAG, "screen sharing request success")
        viewCallbacks.forEach(Consumer { obj: ViewCallback -> obj.onScreenSharingRequestSuccess() })
    }

    fun onResume(context: Context?) {
        // spam all the time otherwise no way to end screen sharing
        if (hasPendingScreenSharingRequest) {
            if (!hasScreenSharingNotificationChannelEnabledUseCase.invoke()) {
                dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog()
            } else {
                onScreenSharingAccepted(context)
            }
        }
    }

    fun onDestroy() {
        Logger.d(TAG, "onDestroy")
        repository.onDestroy()
    }

    val isSharingScreen: Boolean
        get() = repository.isSharingScreen

    fun onScreenSharingAccepted(context: Context?) {
        Logger.d(TAG, "onScreenSharingAccepted")
        dialogController.dismissCurrentDialog()
        showScreenSharingEnabledNotification()
        repository.onScreenSharingAccepted(
            Utils.getActivity(context),
            gliaSdkConfigurationManager.screenSharingMode
        )
        hasPendingScreenSharingRequest = false
    }

    fun onScreenSharingAcceptedAndPermissionAsked(context: Context?) {
        Logger.d(TAG, "onScreenSharingAcceptedAndPermissionAsked")
        dialogController.dismissCurrentDialog()
        showScreenSharingEnabledNotification()
        repository.onScreenSharingAcceptedAndPermissionAsked(
            Utils.getActivity(context),
            gliaSdkConfigurationManager.screenSharingMode
        )
        hasPendingScreenSharingRequest = false
    }

    fun onScreenSharingDeclined() {
        Logger.d(TAG, "onScreenSharingDeclined")
        dialogController.dismissCurrentDialog()
        repository.onScreenSharingDeclined()
        hasPendingScreenSharingRequest = false
    }

    fun onScreenSharingNotificationEndPressed() {
        hideScreenSharingEnabledNotification()
        repository.onEndScreenSharing()
    }

    fun setViewCallback(callback: ViewCallback?) {
        callback?.run { viewCallbacks.add(this) }
    }

    fun removeViewCallback(callback: ViewCallback?) {
        viewCallbacks.remove(callback)
    }

    private fun showScreenSharingEnabledNotification() {
        showScreenSharingNotificationUseCase.invoke()
    }

    private fun hideScreenSharingEnabledNotification() {
        removeScreenSharingNotificationUseCase.invoke()
    }

    interface ViewCallback {
        fun onScreenSharingRequestError(ex: GliaException?) {}
        fun onScreenSharingRequestSuccess() {}
        fun onScreenSharingStarted() {}
    }

    override fun onForceStopScreenSharing() {
        repository.forceEndScreenSharing()
        removeScreenSharingNotificationUseCase.invoke()
    }
}