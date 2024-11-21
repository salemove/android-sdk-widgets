package com.glia.widgets.helper

internal sealed class Data<out T> {
    val hasValue: Boolean get() = this is Value
    val valueOrNull: T? get() = (this as? Value)?.result

    data class Value<out T>(val result: T) : Data<T>()
    data object Empty : Data<Nothing>()

    companion object
}

internal fun <T : Any> Data.Companion.from(value: T?): Data<T> = when (value) {
    null -> Data.Empty
    else -> Data.Value(value)
}
