package com.glia.widgets.operator

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CheckMediaUpgradePermissionsUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.engagement.domain.OperatorMediaUpgradeOfferUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.isAudio
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.dialog.holder.DialogHolderActivity
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

internal class RequestHandlerController(
    operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val checkMediaUpgradePermissionsUseCase: CheckMediaUpgradePermissionsUseCase
) : RequestHandlerContract.Controller {

    private val _state: PublishProcessor<RequestHandlerContract.State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<RequestHandlerContract.State>> = _state.onBackpressureLatest().map(::OneTimeEvent)

    init {
        operatorMediaUpgradeOfferUseCase().unSafeSubscribe(::handleMediaUpgradeOffer)
        acceptMediaUpgradeOfferUseCase.result.unSafeSubscribe(::handleMediaUpgradeOfferAcceptResult)
    }

    private fun handleMediaUpgradeOfferAcceptResult(mediaUpgradeOffer: MediaUpgradeOffer) {
        val mediaType = if (mediaUpgradeOffer.isAudio) Engagement.MediaType.AUDIO else Engagement.MediaType.VIDEO
        _state.onNext(RequestHandlerContract.State.OpenCallActivity(mediaType))
    }

    private fun handleMediaUpgradeOffer(mediaUpgradeOfferData: MediaUpgradeOfferData) {
        _state.onNext(RequestHandlerContract.State.RequestMediaUpgrade(mediaUpgradeOfferData))
    }

    override fun onMediaUpgradeAccepted(offer: MediaUpgradeOffer, activity: Activity) {
        dismissAlertDialog()
        checkMediaUpgradePermissionsUseCase(offer) { granted ->
            finishIfDialogHolder(activity)
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
        finishIfDialogHolder(activity)
        declineMediaUpgradeOfferUseCase(offer)
    }

    private fun finishIfDialogHolder(activity: Activity) {
        if (activity is DialogHolderActivity) {
            activity.finish()
        }
    }

    private fun dismissAlertDialog() {
        _state.onNext(RequestHandlerContract.State.DismissAlertDialog)
    }
}
