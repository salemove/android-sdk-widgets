package com.glia.widgets.callvisualizer.controller

import com.glia.widgets.core.dialog.DialogContract
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.engagement.domain.EngagementRequestUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.helper.unSafeSubscribe
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor
import com.glia.widgets.engagement.State as EngagementState

internal interface CallVisualizerContract {

    sealed interface State {
        object DisplayVisitorCodeDialog : State
        data class DisplayConfirmationDialog(val links: ConfirmationDialogLinks) : State
        object DismissDialog : State
        object CloseHolderActivity : State
        data class OpenWebBrowserScreen(val title: String, val url: String) : State
    }

    interface Controller {
        val state: Flowable<OneTimeEvent<State>>

        val engagementStartFlow: Flowable<EngagementState>
        val engagementEndFlow: Flowable<EngagementState>
        fun onEngagementConfirmationDialogAllowed()
        fun onEngagementConfirmationDialogDeclined()
        fun saveVisitorContextAssetId(visitorContextAssetId: String)
        fun showVisitorCodeDialog()
        fun onLinkClicked(link: Link)
        fun dismissVisitorCodeDialog()
        fun onWebBrowserOpened()
    }
}

internal class CallVisualizerController(
    private val dialogController: DialogContract.Controller,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    private val engagementRequestUseCase: EngagementRequestUseCase,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase
) : CallVisualizerContract.Controller {

    private val _state: PublishProcessor<CallVisualizerContract.State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<CallVisualizerContract.State>> = _state.asOneTimeStateFlowable()
    override val engagementStartFlow: Flowable<EngagementState> get() = engagementStateUseCase().filter { it is EngagementState.StartedCallVisualizer }
    override val engagementEndFlow: Flowable<EngagementState> get() = engagementStateUseCase().filter { it is EngagementState.FinishedCallVisualizer }

    private var visitorContextAssetId: String? = null

    init {
        registerCallVisualizerListeners()
    }

    private fun registerCallVisualizerListeners() {
        engagementRequestUseCase().unSafeSubscribe { onEngagementRequested() }
        dialogController.addCallback(::handleDialogState)
        engagementStartFlow.unSafeSubscribe { dismissVisitorCodeDialog() }
    }

    private fun handleDialogState(dialogState: DialogState) {
        when (dialogState) {
            is DialogState.VisitorCode -> _state.onNext(CallVisualizerContract.State.DisplayVisitorCodeDialog)
            is DialogState.None -> _state.onNext(CallVisualizerContract.State.DismissDialog)
            is DialogState.CVConfirmation -> _state.onNext(
                CallVisualizerContract.State.DisplayConfirmationDialog(
                    confirmationDialogLinksUseCase()
                )
            )

            else -> {
                //no-op
            }
        }
    }

    override fun showVisitorCodeDialog() {
        dialogController.showVisitorCodeDialog()
    }

    private fun onEngagementRequested() {
        dialogController.dismissVisitorCodeDialog()

        confirmationDialogUseCase { shouldShow ->
            if (shouldShow) {
                dialogController.showCVEngagementConfirmationDialog()
            } else {
                engagementRequestUseCase.accept(visitorContextAssetId.orEmpty())
            }
        }
    }

    override fun onLinkClicked(link: Link) {
        dialogController.dismissCurrentDialog()
        _state.onNext(CallVisualizerContract.State.OpenWebBrowserScreen(link.title, link.url))
    }

    override fun onEngagementConfirmationDialogAllowed() {
        closeHolderActivity()
        engagementRequestUseCase.accept(visitorContextAssetId.orEmpty())
        dialogController.dismissCurrentDialog()
    }

    override fun onEngagementConfirmationDialogDeclined() {
        closeHolderActivity()
        engagementRequestUseCase.decline()
        dialogController.dismissCurrentDialog()
    }

    override fun dismissVisitorCodeDialog() {
        dialogController.dismissVisitorCodeDialog()
        closeHolderActivity()
    }

    override fun onWebBrowserOpened() {
        dialogController.showCVEngagementConfirmationDialog()
    }

    private fun closeHolderActivity() {
        _state.onNext(CallVisualizerContract.State.CloseHolderActivity)
    }

    override fun saveVisitorContextAssetId(visitorContextAssetId: String) {
        this.visitorContextAssetId = visitorContextAssetId
    }
}
