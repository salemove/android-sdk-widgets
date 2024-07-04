package com.glia.widgets.messagecenter

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import kotlin.properties.Delegates

/**
 * Base interface for stateful widgets adapter
 * [S] - State
 * [T] - Theme
 */
internal interface StatefulWidgetAdapter<S : Enum<S>, T : Mergeable<T>> {

    val callback: StatefulWidgetAdapterCallback<S, T>

    fun updateStatefulTheme(newStatefulTheme: Map<out S, T?>)

    fun updateState(state: S)
}

/**
 * Callback to get Theme and State updates from [StatefulWidgetAdapter]
 */
internal fun interface StatefulWidgetAdapterCallback<S : Enum<S>, T : Mergeable<T>> {
    fun onNewTheme(theme: T)
    fun onNewState(newState: S) {}
}

internal class SimpleStatefulWidgetAdapter<S : Enum<S>, T : Mergeable<T>>(
    defaultState: S,
    override val callback: StatefulWidgetAdapterCallback<S, T>
) : StatefulWidgetAdapter<S, T> {

    private val states: Array<out S> by lazy { defaultState::class.java.enumConstants }

    private var state: S by Delegates.vetoable(defaultState) { _, oldState, newState ->
        validateState(oldState, newState)
    }

    private var statefulTheme: Map<out S, T?> by Delegates.vetoable(emptyMap()) { _, oldThemes, newThemes ->
        validateThemes(oldThemes, newThemes)
    }

    private fun validateState(oldState: S, newState: S): Boolean {
        if (oldState != newState) {
            onStateChanged(newState)
            return true
        }
        return false
    }

    private fun validateThemes(oldThemes: Map<out S, T?>, newThemes: Map<out S, T?>): Boolean {
        if (oldThemes != newThemes) {
            onStatefulThemeChanged(newThemes)
            return true
        }

        return false
    }

    private fun onStatefulThemeChanged(newThemes: Map<out S, T?>) {
        callback.onNewTheme(newThemes[state] ?: return)
    }

    private fun onStateChanged(newState: S) {
        callback.onNewState(newState)
        callback.onNewTheme(statefulTheme[newState] ?: return)
    }

    override fun updateStatefulTheme(newStatefulTheme: Map<out S, T?>) {
        statefulTheme = when {
            statefulTheme.isEmpty() -> newStatefulTheme
            statefulTheme == newStatefulTheme -> return
            else -> composeHybridTheme(newStatefulTheme)
        }
    }

    override fun updateState(state: S) {
        this.state = state
    }

    private fun composeHybridTheme(newStatefulTheme: Map<out S, T?>): Map<S, T?> =
        states.associate { composeThemeForState(it, newStatefulTheme) }

    private fun composeThemeForState(state: S, newStatefulTheme: Map<out S, T?>): Pair<S, T?> =
        state to statefulTheme[state].merge(newStatefulTheme[state])
}
