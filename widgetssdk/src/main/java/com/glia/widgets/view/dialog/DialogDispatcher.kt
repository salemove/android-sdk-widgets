package com.glia.widgets.view.dialog

import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.view.dialog.DialogDispatcher.State.DismissDialog
import com.glia.widgets.view.dialog.DialogDispatcher.State.NotificationPermissionDialog
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

internal interface DialogDispatcher {
    val state: Flowable<OneTimeEvent<State>>

    fun showNotificationPermissionDialog(onAllow: () -> Unit, onCancel: () -> Unit = {})
    fun dismissDialog()

    sealed interface State {
        data object DismissDialog : State
        data class NotificationPermissionDialog(val onAllow: () -> Unit, val onCancel: () -> Unit) : State
    }
}

internal class DialogDispatcherImpl : DialogDispatcher {
    private val _state: PublishProcessor<DialogDispatcher.State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<DialogDispatcher.State>> = _state.asOneTimeStateFlowable()

    override fun showNotificationPermissionDialog(onAllow: () -> Unit, onCancel: () -> Unit) {
        _state.onNext(NotificationPermissionDialog(onAllow, onCancel))
    }

    override fun dismissDialog() {
        _state.onNext(DismissDialog)
    }
}
