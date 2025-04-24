package com.glia.widgets.view.dialog

import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.view.dialog.GlobalDialogController.State.DismissDialog
import com.glia.widgets.view.dialog.GlobalDialogController.State.NotificationPermissionDialog
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

internal interface GlobalDialogController {
    val state: Flowable<OneTimeEvent<State>>

    fun showNotificationPermissionDialog(onAllow: () -> Unit, onCancel: () -> Unit)
    fun dismissDialog()

    sealed interface State {
        data object DismissDialog : State
        data class NotificationPermissionDialog(val onAllow: () -> Unit, val onCancel: () -> Unit) : State
    }
}

internal class GlobalDialogControllerImpl : GlobalDialogController {
    private val _state: PublishProcessor<GlobalDialogController.State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<GlobalDialogController.State>> = _state.asOneTimeStateFlowable()

    override fun showNotificationPermissionDialog(onAllow: () -> Unit, onCancel: () -> Unit) {
        _state.onNext(NotificationPermissionDialog(onAllow, onCancel))
    }

    override fun dismissDialog() {
        _state.onNext(DismissDialog)
    }
}
