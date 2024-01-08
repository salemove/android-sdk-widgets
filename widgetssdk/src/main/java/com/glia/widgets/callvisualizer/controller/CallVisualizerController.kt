package com.glia.widgets.callvisualizer.controller

import android.app.Activity
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.CurrentOperatorUseCase
import com.glia.widgets.engagement.domain.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.EngagementRequestUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.MediaUpgradeOfferUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.formattedName
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.Flowable

internal interface CallVisualizerController {

    val engagementStartFlow: Flowable<State>
    val engagementEndFlow: Flowable<State>
    val acceptMediaUpgradeOfferResult: Flowable<MediaUpgradeOffer>
    fun acceptMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun declineMediaUpgradeRequest(offer: MediaUpgradeOffer)
    fun onEngagementConfirmationDialogAllowed()
    fun onEngagementConfirmationDialogDeclined()
    fun saveVisitorContextAssetId(visitorContextAssetId: String)
    fun isCallOrChatScreenActive(resumedActivity: Activity?): Boolean
}

internal class CallVisualizerControllerImpl(
    private val dialogController: DialogController,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    private val isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase,
    private val mediaUpgradeOfferUseCase: MediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val engagementRequestUseCase: EngagementRequestUseCase,
    private val currentOperatorUseCase: CurrentOperatorUseCase,
    private val engagementStateUseCase: EngagementStateUseCase
) : CallVisualizerController {

    override val engagementStartFlow: Flowable<State> get() = engagementStateUseCase().filter { it is State.StartedCallVisualizer }
    override val engagementEndFlow: Flowable<State> get() = engagementStateUseCase().filter { it is State.FinishedCallVisualizer }

    override val acceptMediaUpgradeOfferResult = acceptMediaUpgradeOfferUseCase.resultForCallVisualizer

    private var visitorContextAssetId: String? = null

    init {
        registerCallVisualizerListeners()
    }

    private fun registerCallVisualizerListeners() {
        mediaUpgradeOfferUseCase().withLatestFrom(currentOperatorUseCase()) { offer, operator -> offer to operator.formattedName }.unSafeSubscribe {
            Logger.d(TAG, "upgradeOfferConsumer, offer: ${it.first}")
            if (it.first.video == MediaDirection.TWO_WAY) {
                onTwoWayMediaUpgradeRequest(it.first, it.second)
            } else if (it.first.video == MediaDirection.ONE_WAY) {
                onOneWayMediaUpgradeRequest(it.first, it.second)
            }
        }
        engagementRequestUseCase().unSafeSubscribe { onEngagementRequested() }
    }

    override fun isCallOrChatScreenActive(resumedActivity: Activity?): Boolean = isCallOrChatScreenActiveUseCase(resumedActivity)

    override fun acceptMediaUpgradeRequest(offer: MediaUpgradeOffer) {
        acceptMediaUpgradeOfferUseCase(offer)
    }

    override fun declineMediaUpgradeRequest(offer: MediaUpgradeOffer) {
        declineMediaUpgradeOfferUseCase(offer)
    }

    private fun onOneWayMediaUpgradeRequest(mediaUpgradeOffer: MediaUpgradeOffer, operatorNameFormatted: String) {
        dialogController.showUpgradeVideoDialog1Way(mediaUpgradeOffer, operatorNameFormatted)
    }

    private fun onTwoWayMediaUpgradeRequest(mediaUpgradeOffer: MediaUpgradeOffer, operatorNameFormatted: String) {
        dialogController.showUpgradeVideoDialog2Way(mediaUpgradeOffer, operatorNameFormatted)
    }

    private fun onEngagementRequested() {
        dialogController.dismissVisitorCodeDialog()

        confirmationDialogUseCase { shouldShow ->
            if (shouldShow) {
                dialogController.showEngagementConfirmationDialog()
            } else {
                engagementRequestUseCase.accept(visitorContextAssetId.orEmpty())
            }
        }
    }

    override fun onEngagementConfirmationDialogAllowed() {
        engagementRequestUseCase.accept(visitorContextAssetId.orEmpty())
    }

    override fun onEngagementConfirmationDialogDeclined() {
        engagementRequestUseCase.decline()
    }

    override fun saveVisitorContextAssetId(visitorContextAssetId: String) {
        this.visitorContextAssetId = visitorContextAssetId
    }
}
