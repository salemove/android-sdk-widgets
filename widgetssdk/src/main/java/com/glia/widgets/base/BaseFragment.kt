package com.glia.widgets.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * Base Fragment for SDK screens with MVI architecture.
 *
 * Automatically collects state and effects from the ViewModel and delegates to
 * [handleState] and [handleEffect] methods.
 *
 * @param State UI state type implementing [UiState]
 * @param Effect One-time effect type implementing [UiEffect]
 * @param VM ViewModel type extending [BaseViewModel]
 *
 * Usage:
 * ```
 * class SurveyFragment : BaseFragment<SurveyUiState, SurveyEffect, SurveyViewModel>(R.layout.survey) {
 *     override val viewModel: SurveyViewModel by viewModels { Dependencies.viewModelFactory }
 *
 *     override fun handleState(state: SurveyUiState) {
 *         surveyView.renderState(state)
 *     }
 *
 *     override fun handleEffect(effect: SurveyEffect) {
 *         when (effect) {
 *             SurveyEffect.Dismiss -> dismiss()
 *         }
 *     }
 * }
 * ```
 */
internal abstract class BaseFragment<State : UiState, Effect : UiEffect, VM : BaseViewModel<State, *, Effect>>(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId) {

    /**
     * The ViewModel instance for this Fragment.
     * Subclasses should use `by viewModels { Dependencies.viewModelFactory }` to initialize.
     */
    protected abstract val viewModel: VM

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
     * Collects the Flow when the Fragment is at least in STARTED state.
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
