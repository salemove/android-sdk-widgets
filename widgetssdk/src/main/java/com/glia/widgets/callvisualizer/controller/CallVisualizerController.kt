package com.glia.widgets.callvisualizer.controller

import android.app.Activity
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementRequestUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.Flowable

internal interface CallVisualizerContract {
    interface Controller {
        val engagementStartFlow: Flowable<State>
        val engagementEndFlow: Flowable<State>
        fun onEngagementConfirmationDialogAllowed()
        fun onEngagementConfirmationDialogDeclined()
        fun saveVisitorContextAssetId(visitorContextAssetId: String)
        fun isCallOrChatScreenActive(resumedActivity: Activity?): Boolean
    }
}

internal class CallVisualizerController(
    private val dialogController: DialogContract.Controller,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    private val isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase,
    private val engagementRequestUseCase: EngagementRequestUseCase,
    private val engagementStateUseCase: EngagementStateUseCase
) : CallVisualizerContract.Controller {

    override val engagementStartFlow: Flowable<State> get() = engagementStateUseCase().filter { it is State.StartedCallVisualizer }
    override val engagementEndFlow: Flowable<State> get() = engagementStateUseCase().filter { it is State.FinishedCallVisualizer }

    private var visitorContextAssetId: String? = null

    init {
        registerCallVisualizerListeners()
    }

    private fun registerCallVisualizerListeners() {
        engagementRequestUseCase().unSafeSubscribe { onEngagementRequested() }
    }

    override fun isCallOrChatScreenActive(resumedActivity: Activity?): Boolean = isCallOrChatScreenActiveUseCase(resumedActivity)

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
