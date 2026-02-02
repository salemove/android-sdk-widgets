package com.glia.widgets.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing MVI pattern.
 *
 * @param State Immutable UI state data class implementing [UiState]
 * @param Intent User actions/events implementing [UiIntent]
 * @param Effect One-time side effects (navigation, toasts) implementing [UiEffect]
 *
 * Usage:
 * ```
 * class SurveyViewModel : BaseViewModel<SurveyUiState, SurveyIntent, SurveyEffect>(SurveyUiState()) {
 *     override suspend fun handleIntent(intent: SurveyIntent) {
 *         when (intent) {
 *             is SurveyIntent.Submit -> handleSubmit()
 *         }
 *     }
 * }
 * ```
 */
internal abstract class BaseViewModel<State : UiState, Intent : UiIntent, Effect : UiEffect>(
    initialState: State
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect: Channel<Effect> = Channel(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    protected val currentState: State get() = _state.value

    fun processIntent(intent: Intent) {
        viewModelScope.launch { handleIntent(intent) }
    }

    protected abstract suspend fun handleIntent(intent: Intent)

    protected fun updateState(reducer: State.() -> State) {
        _state.update { it.reducer() }
    }

    protected suspend fun emitEffect(effect: Effect) {
        _effect.send(effect)
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
