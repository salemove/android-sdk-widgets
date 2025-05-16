package com.glia.widgets.view.dialog

import androidx.annotation.StringRes
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.asOneTimeStateFlowable
import com.glia.widgets.view.dialog.UiComponentsDispatcher.State.DismissDialog
import com.glia.widgets.view.dialog.UiComponentsDispatcher.State.NotificationPermissionDialog
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

internal interface UiComponentsDispatcher {
    val state: Flowable<OneTimeEvent<State>>

    // Dialog
    fun showNotificationPermissionDialog(onAllow: () -> Unit, onCancel: () -> Unit = {})
    fun dismissDialog()

    // Snackbar
    fun showSnackBar(@StringRes messageResId: Int)

    // Activity
    fun launchSCTranscriptActivity()

    sealed interface State {
        data object DismissDialog : State
        data class NotificationPermissionDialog(val onAllow: () -> Unit, val onCancel: () -> Unit) : State
        data class ShowSnackBar(@StringRes val messageResId: Int) : State
        data object LaunchSCTranscriptActivity : State
    }
}

internal class UiComponentsDispatcherImpl : UiComponentsDispatcher {
    private val _state: PublishProcessor<UiComponentsDispatcher.State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<UiComponentsDispatcher.State>> = _state.asOneTimeStateFlowable()

    override fun showNotificationPermissionDialog(onAllow: () -> Unit, onCancel: () -> Unit) =
        _state.onNext(NotificationPermissionDialog(onAllow, onCancel))

    override fun dismissDialog() = _state.onNext(DismissDialog)

    override fun showSnackBar(messageResId: Int) = _state.onNext(UiComponentsDispatcher.State.ShowSnackBar(messageResId))

    override fun launchSCTranscriptActivity() = _state.onNext(UiComponentsDispatcher.State.LaunchSCTranscriptActivity)
}
