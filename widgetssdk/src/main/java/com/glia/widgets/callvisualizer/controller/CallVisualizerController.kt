package com.glia.widgets.callvisualizer.controller

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.engagement.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.CurrentOperatorUseCase
import com.glia.widgets.engagement.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.EngagementRequestUseCase
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.MediaUpgradeOfferUseCase
import com.glia.widgets.engagement.State
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.formattedName
import io.reactivex.Flowable

internal class CallVisualizerController(
    private val dialogController: DialogController,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    @get:VisibleForTesting val isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase,
    private val mediaUpgradeOfferUseCase: MediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val engagementRequestUseCase: EngagementRequestUseCase,
    private val currentOperatorUseCase: CurrentOperatorUseCase,
    private val engagementStateUseCase: EngagementStateUseCase
) {

    val engagementStartFlow: Flowable<State> get() = engagementStateUseCase().filter { it is State.StartedCallVisualizer }
    val engagementEndFlow: Flowable<State> get() = engagementStateUseCase().filter { it is State.FinishedCallVisualizer }

    val acceptMediaUpgradeOfferResult = acceptMediaUpgradeOfferUseCase.resultForCallVisualizer

    private var visitorContextAssetId: String? = null

    fun init() {
        Logger.d(TAG, "CallVisualizerController initialized")
        registerCallVisualizerListeners()
    }

    @SuppressLint("CheckResult")
    private fun registerCallVisualizerListeners() {
        mediaUpgradeOfferUseCase().withLatestFrom(currentOperatorUseCase()) { offer, operator ->
            offer to operator.formattedName
        }.subscribe {
            Logger.d(TAG, "upgradeOfferConsumer, offer: ${it.first}")
            if (it.first.video == MediaDirection.TWO_WAY) {
                onTwoWayMediaUpgradeRequest(it.first, it.second)
            } else if (it.first.video == MediaDirection.ONE_WAY) {
                onOneWayMediaUpgradeRequest(it.first, it.second)
            }
        }
        engagementRequestUseCase().subscribe { onEngagementRequested() }
    }

    fun acceptMediaUpgradeRequest(offer: MediaUpgradeOffer) {
        acceptMediaUpgradeOfferUseCase(offer)
    }

    fun declineMediaUpgradeRequest(offer: MediaUpgradeOffer) {
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

    fun onEngagementConfirmationDialogAllowed() {
        engagementRequestUseCase.accept(visitorContextAssetId.orEmpty())
    }

    fun onEngagementConfirmationDialogDeclined() {
        engagementRequestUseCase.decline()
    }

    fun saveVisitorContextAssetId(visitorContextAssetId: String) {
        this.visitorContextAssetId = visitorContextAssetId
    }
}
