package com.glia.widgets.helper

internal sealed class Data<out T> {
    val hasValue: Boolean get() = this is Value
    val valueOrNull: T? get() = (this as? Value)?.result

    data class Value<out T>(val result: T) : Data<T>()
    object Empty : Data<Nothing>()
}
