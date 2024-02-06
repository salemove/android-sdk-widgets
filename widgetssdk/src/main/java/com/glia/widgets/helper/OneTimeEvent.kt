package com.glia.widgets.helper

internal data class OneTimeEvent<out T>(val value: T) {
    var consumed: Boolean = false
        private set

    fun markConsumed() {
        consumed = true
    }

    fun consume(consumeCallback: T.() -> Unit) {
        markConsumed()
        consumeCallback(value)
    }

}
