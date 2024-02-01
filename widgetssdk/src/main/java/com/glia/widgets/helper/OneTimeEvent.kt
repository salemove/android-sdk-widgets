package com.glia.widgets.helper

internal data class OneTimeEvent<T>(private val value: T) {
    private var _isConsumed: Boolean = false

    fun consume(): T {
        val captured = checkNotNull(view()) { "value was consumed before" }
        markConsumed()
        return captured
    }

    fun markConsumed() {
        _isConsumed = true
    }

    fun view(): T? = if (_isConsumed) null else value
}
