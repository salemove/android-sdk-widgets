package com.glia.widgets.operator

import android.annotation.SuppressLint
import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.helper.isAudio
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.domain.SetOverlayPermissionRequestDialogShownUseCase
import com.glia.widgets.internal.dialog.model.DialogState
import com.glia.widgets.operator.OperatorRequestContract.State
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

//This is in fact singleton
@SuppressLint("CheckResult")
internal class OperatorRequestController(
    operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val checkMediaUpgradePermissionsUseCase: CheckMediaUpgradePermissionsUseCase,
    private val setOverlayPermissionRequestDialogShownUseCase: SetOverlayPermissionRequestDialogShownUseCase,
    private val dialogController: DialogContract.Controller,
) : OperatorRequestContract.Controller {

    private val _state: PublishProcessor<State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<State>> = _state.asOneTimeStateFlowable()
    private val dialogCallback: DialogContract.Controller.Callback = DialogContract.Controller.Callback(::handleDialogCallback)


    init {
        operatorMediaUpgradeOfferUseCase().subscribe(::handleMediaUpgradeOffer)
        acceptMediaUpgradeOfferUseCase.result.subscribe(::handleMediaUpgradeOfferAcceptResult)
        dialogController.addCallback(dialogCallback)
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
