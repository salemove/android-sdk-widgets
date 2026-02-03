package com.glia.widgets.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Base DialogFragment with MVI architecture and edge-to-edge support.
 *
 * Automatically collects state and effects from the ViewModel and delegates to
 * [handleState] and [handleEffect] methods.
 *
 * This is similar to [BaseBottomSheetFragment] but for full-screen or centered dialogs
 * instead of bottom sheets.
 *
 * @param State UI state type implementing [UiState]
 * @param Effect One-time effect type implementing [UiEffect]
 * @param VM ViewModel type extending [BaseViewModel]
 */
internal abstract class BaseDialogFragment<State : UiState, Effect : UiEffect, VM : BaseViewModel<State, *, Effect>> :
    AppCompatDialogFragment() {

    /**
     * The ViewModel instance for this Fragment.
     * Subclasses should use `by viewModels { Dependencies.viewModelFactory }` to initialize.
     */
    protected abstract val viewModel: VM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            // Enable edge-to-edge for the dialog
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        collectState()
        collectEffects()
    }

    /**
     * Called after view is created. Override to set up view listeners and initial configuration.
     */
    protected open fun setupViews() {}

    /**
     * Called when new state is emitted from the ViewModel.
     * Override to render the state to the UI.
     */
    protected abstract fun handleState(state: State)

    /**
     * Called when a one-time effect is emitted from the ViewModel.
     * Override to handle navigation, toasts, dialogs, etc.
     */
    protected abstract fun handleEffect(effect: Effect)

    private fun collectState() {
        viewModel.state.collectWithLifecycle { state ->
            handleState(state)
        }
    }

    private fun collectEffects() {
        viewModel.effect.collectWithLifecycle { effect ->
            handleEffect(effect)
        }
    }

    /**
     * Extension function for lifecycle-aware Flow collection.
     */
    protected fun <T> Flow<T>.collectWithLifecycle(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ) {
        flowWithLifecycle(viewLifecycleOwner.lifecycle, state)
            .onEach(action)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}