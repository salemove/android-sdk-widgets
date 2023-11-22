package com.glia.widgets.helper

sealed class Data<out T> {
    val hasValue: Boolean get() = this is Value

    data class Value<out T>(val result: T) : Data<T>()
    object Empty : Data<Nothing>()
}
