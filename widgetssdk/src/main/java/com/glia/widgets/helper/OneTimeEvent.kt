package com.glia.widgets.helper

internal data class OneTimeEvent<T>(private val value: T) {
    private var _isConsumed: Boolean = false

    fun consume(): T? {
        markConsumed()
        return view()
    }

    fun markConsumed() {
        _isConsumed = true
    }

    fun view(): T? = if (_isConsumed) null else value
}
