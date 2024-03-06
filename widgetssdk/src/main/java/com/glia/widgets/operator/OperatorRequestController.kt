package com.glia.widgets.operator

import android.app.Activity
import android.app.Activity.RESULT_OK
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.notification.domain.RemoveScreenSharingNotificationUseCase
import com.glia.widgets.core.notification.domain.ShowScreenSharingNotificationUseCase
import com.glia.widgets.core.permissions.domain.HasScreenSharingNotificationChannelEnabledUseCase
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.helper.isAudio
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.operator.OperatorRequestContract.State
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

internal class OperatorRequestController(
    operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val checkMediaUpgradePermissionsUseCase: CheckMediaUpgradePermissionsUseCase,
    private val screenSharingUseCase: ScreenSharingUseCase,
    private val hasScreenSharingNotificationChannelEnabledUseCase: HasScreenSharingNotificationChannelEnabledUseCase,
    private val currentOperatorUseCase: CurrentOperatorUseCase,
    private val showScreenSharingNotificationUseCase: ShowScreenSharingNotificationUseCase,
    private val removeScreenSharingNotificationUseCase: RemoveScreenSharingNotificationUseCase,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase,
    private val dialogController: DialogContract.Controller,
    private val gliaSdkConfigurationManager: GliaSdkConfigurationManager
) : OperatorRequestContract.Controller {

    private val _state: PublishProcessor<State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<State>> = _state.asOneTimeStateFlowable()
    private val dialogCallback: DialogContract.Controller.Callback = DialogContract.Controller.Callback(::handleDialogCallback)

    init {
        operatorMediaUpgradeOfferUseCase().unSafeSubscribe(::handleMediaUpgradeOffer)
        acceptMediaUpgradeOfferUseCase.result.unSafeSubscribe(::handleMediaUpgradeOfferAcceptResult)
        screenSharingUseCase().unSafeSubscribe(::handleScreenSharingState)
        dialogController.addCallback(dialogCallback)
    }

    private fun handleScreenSharingState(state: ScreenSharingState) {
        when (state) {
            ScreenSharingState.Ended -> removeScreenSharingNotificationUseCase()
            is ScreenSharingState.FailedToAcceptRequest -> onScreenSharingFailedToAcceptRequest(state)
            is ScreenSharingState.Requested -> onScreenSharingRequested()
            else -> {
                //no-op
            }
        }
    }

    private fun onScreenSharingFailedToAcceptRequest(state: ScreenSharingState.FailedToAcceptRequest) {
        removeScreenSharingNotificationUseCase()
        _state.onNext(State.DisplayToast(state.message))
    }

    private fun showCvDialogIfRequired(activity: Activity) {
        if (isCurrentEngagementCallVisualizerUseCase() && isShowOverlayPermissionRequestDialogUseCase()) {
            dialogController.showCVOverlayPermissionDialog()
        } else {
            finishIfDialogHolderActivity(activity)
        }
    }

    private fun onScreenSharingRequested() {
        if (!hasScreenSharingNotificationChannelEnabledUseCase()) {
            dialogController.showEnableScreenSharingNotificationsAndStartSharingDialog()
        } else {
            dialogController.showStartScreenSharingDialog()
        }
    }

    private fun handleMediaUpgradeOfferAcceptResult(mediaUpgradeOffer: MediaUpgradeOffer) {
        val mediaType = if (mediaUpgradeOffer.isAudio) Engagement.MediaType.AUDIO else Engagement.MediaType.VIDEO
        _state.onNext(State.OpenCallActivity(mediaType))
    }

    private fun handleMediaUpgradeOffer(mediaUpgradeOfferData: MediaUpgradeOfferData) {
        dialogController.showUpgradeDialog(mediaUpgradeOfferData)
    }

    private fun handleDialogCallback(dialogState: DialogState) {
        when (dialogState) {
            is DialogState.MediaUpgrade -> _state.onNext(State.RequestMediaUpgrade(dialogState.data))
            is DialogState.None -> _state.onNext(State.DismissAlertDialog)
            is DialogState.EnableScreenSharingNotificationsAndStartSharing -> _state.onNext(State.EnableScreenSharingNotificationsAndStartSharing)
            is DialogState.StartScreenSharing -> _state.onNext(State.ShowScreenSharingDialog(currentOperatorUseCase.formattedNameValue))
            is DialogState.CVOverlayPermission -> _state.onNext(State.ShowOverlayDialog)
            else -> {
                //no-op
            }
        }
    }

    override fun onMediaUpgradeAccepted(offer: MediaUpgradeOffer, activity: Activity) {
        dismissAlertDialog()
        checkMediaUpgradePermissionsUseCase(offer) { granted ->
            finishIfDialogHolderActivity(activity)
            onMediaUpgradePermissionResult(offer, granted)
        }
    }

    private fun onMediaUpgradePermissionResult(offer: MediaUpgradeOffer, granted: Boolean) {
        if (granted) {
            acceptMediaUpgradeOfferUseCase(offer)
        } else {
            declineMediaUpgradeOfferUseCase(offer)
        }
    }

    override fun onMediaUpgradeDeclined(offer: MediaUpgradeOffer, activity: Activity) {
        dismissAlertDialog()
        finishIfDialogHolderActivity(activity)
        declineMediaUpgradeOfferUseCase(offer)
    }

    override fun onShowEnableScreenSharingNotificationsAccepted() {
        dismissAlertDialog()
        _state.onNext(State.OpenNotificationsScreen)
    }

    override fun onShowEnableScreenSharingNotificationsDeclined(activity: Activity) {
        dismissAlertDialog()
        finishIfDialogHolderActivity(activity)
        screenSharingUseCase.declineRequest()
    }

    override fun onScreenSharingDialogAccepted(activity: Activity) {
        dismissAlertDialog()
        _state.onNext(State.AcquireMediaProjectionToken)
        showScreenSharingNotificationUseCase()
        screenSharingUseCase.acceptRequestWithAskedPermission(activity, gliaSdkConfigurationManager.screenSharingMode)
    }

    override fun onScreenSharingDialogDeclined(activity: Activity) {
        dismissAlertDialog()
        finishIfDialogHolderActivity(activity)
        screenSharingUseCase.declineRequest()
    }

    override fun onReturnedFromNotificationScreen() {
        onScreenSharingRequested()
    }

    override fun onNotificationScreenRequested() {
        _state.onNext(State.WaitForNotificationScreenOpen)
    }

    override fun onMediaProjectionResultReceived(result: ActivityResult, activity: ComponentActivity) {
        result.apply {
            if (resultCode == RESULT_OK && data != null) {
                screenSharingUseCase.onActivityResultSkipPermissionRequest(resultCode, data)
                showCvDialogIfRequired(activity)
            } else {
                finishIfDialogHolderActivity(activity)
                screenSharingUseCase.declineRequest()
                removeScreenSharingNotificationUseCase()
            }
        }
    }

    override fun onOverlayPermissionRequestAccepted(activity: Activity) {
        dismissAlertDialog()
        finishIfDialogHolderActivity(activity)
        setOverlayPermissionRequestDialogShownUseCase()
        Logger.d(TAG, "Allowed to request overlay permission ✅")
        _state.onNext(State.OpenOverlayPermissionScreen)
    }

    override fun onOverlayPermissionRequestDeclined(activity: Activity) {
        dismissAlertDialog()
        finishIfDialogHolderActivity(activity)
        setOverlayPermissionRequestDialogShownUseCase()
        Logger.d(TAG, "Declined to request overlay permission ❌")
    }

    override fun overlayPermissionScreenOpened() {
        Logger.d(TAG, "Overlay permission screen opened ✅")
    }

    override fun failedToOpenOverlayPermissionScreen() {
        Logger.d(TAG, "No Activity to open Overlay permission screen ❌")
    }

    override fun onNotificationScreenOpened() {
        _state.onNext(State.WaitForNotificationScreenResult)
    }

    private fun finishIfDialogHolderActivity(activity: Activity) {
        if (activity is DialogHolderActivity) {
            activity.finish()
        }
    }

    private fun dismissAlertDialog() {
        dialogController.dismissCurrentDialog()
    }
}
