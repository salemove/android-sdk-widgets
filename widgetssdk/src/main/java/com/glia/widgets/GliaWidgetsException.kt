package com.glia.widgets

import com.glia.widgets.GliaWidgetsException.Cause
import com.glia.widgets.helper.Logger
import com.glia.androidsdk.GliaException as GliaCoreException

/**
 * Glia Widgets SDK exception that contains a message for debugging and
 * a {@link Cause} enum value for handling some of the errors programmatically.
 */
class GliaWidgetsException internal constructor(val debugMessage: String, val gliaCause: Cause) : RuntimeException(debugMessage) {

    /**
     * Possible exception causes
     */
    enum class Cause {
        INVALID_INPUT,
        INVALID_LOCALE,
        NETWORK_TIMEOUT,
        INTERNAL_ERROR,
        AUTHENTICATION_ERROR,
        PERMISSIONS_DENIED,
        ALREADY_QUEUED,
        FORBIDDEN,
        NOT_MAIN_THREAD,
        FILE_FORMAT_UNSUPPORTED,
        FILE_TOO_LARGE,
        FILE_UNAVAILABLE,
        FILE_UPLOAD_FORBIDDEN,
        QUEUE_CLOSED,
        QUEUE_FULL;
    }

    override fun toString(): String {
        return "GliaWidgetsException: " + this.debugMessage + ", cause: " + gliaCause.toString()
    }
}

internal fun GliaCoreException.toWidgetsType(): GliaWidgetsException = GliaWidgetsException(debugMessage, cause.toWidgetsType())

internal fun GliaCoreException?.toWidgetsType(defaultMessage: String, defaultCause: Cause = Cause.INTERNAL_ERROR): GliaWidgetsException =
    this?.toWidgetsType() ?: let {
        val exception = GliaWidgetsException(defaultMessage, defaultCause)
        Logger.e("GliaWidgetsException", "The Core GliaException is null, using the default error message and cause.", exception)
        return@let exception
    }

private fun GliaCoreException.Cause.toWidgetsType(): Cause =
    when (this) {
        GliaCoreException.Cause.INVALID_INPUT -> Cause.INVALID_INPUT
        GliaCoreException.Cause.INVALID_LOCALE -> Cause.INVALID_LOCALE
        GliaCoreException.Cause.NETWORK_TIMEOUT -> Cause.NETWORK_TIMEOUT
        GliaCoreException.Cause.INTERNAL_ERROR -> Cause.INTERNAL_ERROR
        GliaCoreException.Cause.AUTHENTICATION_ERROR -> Cause.AUTHENTICATION_ERROR
        GliaCoreException.Cause.PERMISSIONS_DENIED -> Cause.PERMISSIONS_DENIED
        GliaCoreException.Cause.ALREADY_QUEUED -> Cause.ALREADY_QUEUED
        GliaCoreException.Cause.FORBIDDEN -> Cause.FORBIDDEN
        GliaCoreException.Cause.NOT_MAIN_THREAD -> Cause.NOT_MAIN_THREAD
        GliaCoreException.Cause.FILE_FORMAT_UNSUPPORTED -> Cause.FILE_FORMAT_UNSUPPORTED
        GliaCoreException.Cause.FILE_TOO_LARGE -> Cause.FILE_TOO_LARGE
        GliaCoreException.Cause.FILE_UNAVAILABLE -> Cause.FILE_UNAVAILABLE
        GliaCoreException.Cause.FILE_UPLOAD_FORBIDDEN -> Cause.FILE_UPLOAD_FORBIDDEN
        GliaCoreException.Cause.QUEUE_CLOSED -> Cause.QUEUE_CLOSED
        GliaCoreException.Cause.QUEUE_FULL -> Cause.QUEUE_FULL
    }

internal fun GliaWidgetsException.toCoreType(): GliaCoreException = GliaCoreException(debugMessage, gliaCause.toCoreType())

private fun Cause.toCoreType(): GliaCoreException.Cause =
    when (this) {
        Cause.INVALID_INPUT -> GliaCoreException.Cause.INVALID_INPUT
        Cause.INVALID_LOCALE -> GliaCoreException.Cause.INVALID_LOCALE
        Cause.NETWORK_TIMEOUT -> GliaCoreException.Cause.NETWORK_TIMEOUT
        Cause.INTERNAL_ERROR -> GliaCoreException.Cause.INTERNAL_ERROR
        Cause.AUTHENTICATION_ERROR -> GliaCoreException.Cause.AUTHENTICATION_ERROR
        Cause.PERMISSIONS_DENIED -> GliaCoreException.Cause.PERMISSIONS_DENIED
        Cause.ALREADY_QUEUED -> GliaCoreException.Cause.ALREADY_QUEUED
        Cause.FORBIDDEN -> GliaCoreException.Cause.FORBIDDEN
        Cause.NOT_MAIN_THREAD -> GliaCoreException.Cause.NOT_MAIN_THREAD
        Cause.FILE_FORMAT_UNSUPPORTED -> GliaCoreException.Cause.FILE_FORMAT_UNSUPPORTED
        Cause.FILE_TOO_LARGE -> GliaCoreException.Cause.FILE_TOO_LARGE
        Cause.FILE_UNAVAILABLE -> GliaCoreException.Cause.FILE_UNAVAILABLE
        Cause.FILE_UPLOAD_FORBIDDEN -> GliaCoreException.Cause.FILE_UPLOAD_FORBIDDEN
        Cause.QUEUE_CLOSED -> GliaCoreException.Cause.QUEUE_CLOSED
        Cause.QUEUE_FULL -> GliaCoreException.Cause.QUEUE_FULL
    }
