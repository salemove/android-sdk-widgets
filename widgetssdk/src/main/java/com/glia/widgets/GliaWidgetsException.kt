package com.glia.widgets

import com.glia.androidsdk.GliaException as GliaCoreException

/**
 * Glia Widgets SDK exception that contains a message for debugging and
 * a {@link Cause} enum value for handling some of the errors programmatically.
 */
class GliaWidgetsException internal constructor(
    val debugMessage: String?,
    val gliaCause: Cause?
) : RuntimeException(
    debugMessage
) {

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

    internal companion object {
        @JvmStatic
        fun from(gliaException: GliaCoreException?): GliaWidgetsException? {
            return gliaException?.let {
                GliaWidgetsException(
                    it.debugMessage,
                    it.cause?.toWidgetsCause()
                )
            }
        }

        private fun GliaCoreException.Cause.toWidgetsCause(): Cause? {
            return try {
                GliaWidgetsException.Cause.valueOf(this.name)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
