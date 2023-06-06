package com.glia.widgets.callvisualizer.mvpsample

abstract class BaseEvent() {
    abstract val tag: String

    fun isFor(tag: String): Boolean {
        return this.tag == tag
    }
}

sealed class SimpleIntEvent: BaseEvent() {
    data class Success(override val tag: String, val int: Int): SimpleIntEvent()
    data class Failure(override val tag: String, val cause: Exception): SimpleIntEvent()
}