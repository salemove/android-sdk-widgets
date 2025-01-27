package com.glia.widgets.view.snackbar

import androidx.annotation.StringRes
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.asOneTimeStateFlowable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

internal interface SnackbarContract {

    sealed interface State {
        data class ShowSnackBar(@StringRes val message: Int) : State
    }

    interface Controller {
        val state: Flowable<OneTimeEvent<State>>

        fun showSnackBar(@StringRes  message: Int)
    }
}

internal class SnackbarController() : SnackbarContract.Controller {

    private val _state: PublishProcessor<SnackbarContract.State> = PublishProcessor.create()
    override val state: Flowable<OneTimeEvent<SnackbarContract.State>> = _state.asOneTimeStateFlowable()

    override fun showSnackBar(@StringRes message: Int) {
        _state.onNext(SnackbarContract.State.ShowSnackBar(message))
    }
}

