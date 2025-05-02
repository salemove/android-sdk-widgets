package com.glia.widgets.operator

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.media.projection.MediaProjectionManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.internal.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import com.glia.widgets.internal.dialog.model.DialogState
import com.glia.widgets.internal.permissions.domain.WithNotificationPermissionUseCase
import com.glia.widgets.core.screensharing.MediaProjectionService
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.PrepareToScreenSharingUseCase
import com.glia.widgets.engagement.domain.ReleaseScreenSharingResourcesUseCase
import com.glia.widgets.engagement.domain.ScreenSharingUseCase
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.helper.isAudio
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.operator.OperatorRequestContract.State
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

internal class OperatorRequestController(
    operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val checkMediaUpgradePermissionsUseCase: CheckMediaUpgradePermissionsUseCase,
    private val screenSharingUseCase: ScreenSharingUseCase,
    private val currentOperatorUseCase: CurrentOperatorUseCase,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase,
    private val dialogController: DialogContract.Controller,
    private val withNotificationPermissionUseCase: WithNotificationPermissionUseCase,
    private val prepareToScreenSharingUseCase: PrepareToScreenSharingUseCase,
    private val releaseScreenSharingResourcesUseCase: ReleaseScreenSharingResourcesUseCase
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
            ScreenSharingState.Ended -> releaseScreenSharingResourcesUseCase()
            is ScreenSharingState.FailedToAcceptRequest -> onScreenSharingFailedToAcceptRequest(state)
            is ScreenSharingState.Requested -> onScreenSharingRequested()
            else -> {
                //no-op
            }
        }
    }

    private fun onScreenSharingFailedToAcceptRequest(state: ScreenSharingState.FailedToAcceptRequest) {
        releaseScreenSharingResourcesUseCase()
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
        dialogController.showStartScreenSharingDialog()
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

    /**
     * Based on Media projection service requirements:
     *
     * After the user accepts the SDK screen-sharing request dialog, we have to follow the following strict steps for media projection flow.
     * 1. Accept the screen-sharing request[ScreenSharingUseCase.acceptRequestWithAskedPermission].
     * The core will prepare resources for the screen-sharing and wait for the media projection result from the widgets to start the projection.
     * 2. Request Media projection permission[State.AcquireMediaProjectionToken].
     * 3. After user approval, start the media projection service[PrepareToScreenSharingUseCase.invoke], [ScreenSharingUseCase.onActivityResultSkipPermissionRequest].
     * 4. Wait until the [MediaProjectionService.startForeground] function is called. **This step is the most important one because otherwise, the system will throw [SecurityException].**
     * 5. Notify core sdk that permission is granted[Engagement.onActivityResult])
     *
     * @see [EngagementRepository.onActivityResultSkipScreenSharingPermissionRequest]
     * @see [EngagementRepository.onReadyToShareScreen]
     * @see <a href="https://github.com/salemove/android-sdk-widgets/pull/1007#:~:text=Media%20projection%20service%20requirements">Media projection service requirements</a>
     *
     * @note Call the [MediaProjectionManager.createScreenCaptureIntent] method before starting the foreground service[MediaProjectionService].
     * Doing so shows a permission notification to the user; the user must grant the permission before you can create the service.
     * After you have created the foreground service, you can call [MediaProjectionManager.getMediaProjection].
     */
    override fun onScreenSharingDialogAccepted(activity: Activity) {
        dismissAlertDialog()

        withNotificationPermissionUseCase {
            _state.onNext(State.AcquireMediaProjectionToken)
            screenSharingUseCase.acceptRequestWithAskedPermission(activity)
        }
    }

    override fun onScreenSharingDialogDeclined(activity: Activity) {
        dismissAlertDialog()
        finishIfDialogHolderActivity(activity)
        screenSharingUseCase.declineRequest()
    }

    override fun onMediaProjectionResultReceived(result: ActivityResult, activity: ComponentActivity) {
        result.apply {
            if (resultCode == RESULT_OK && data != null) {
                prepareToScreenSharingUseCase()
                screenSharingUseCase.onActivityResultSkipPermissionRequest(resultCode, data)
                showCvDialogIfRequired(activity)
            } else {
                finishIfDialogHolderActivity(activity)
                screenSharingUseCase.declineRequest()
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

    private fun finishIfDialogHolderActivity(activity: Activity) {
        if (activity is DialogHolderActivity) {
            activity.finish()
        }
    }

    private fun dismissAlertDialog() {
        dialogController.dismissCurrentDialog()
    }
}
