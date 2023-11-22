package com.glia.widgets.helper

internal data class OneTimeEvent<T>(private val value: T) {
    private var _isConsumed: Boolean = false
    val isConsumed: Boolean get() = _isConsumed

    fun consume(): T {
        markConsumed()
        return value
    }

    fun markConsumed() {
        _isConsumed = true
    }

    fun view(): T = value
}
