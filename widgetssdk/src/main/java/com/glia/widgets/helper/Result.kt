package com.glia.widgets.helper

internal sealed interface Result<out T : Any> {
    val isSuccess: Boolean get() = this is Success

    data class Success<out T : Any>(val value: T) : Result<T>
    data class Failure(val ex: Exception? = null) : Result<Nothing>
}
