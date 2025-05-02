package com.glia.widgets.engagement

/**
 * Defines different media types supported by Glia.
 */
enum class MediaType {
    TEXT,
    AUDIO,
    PHONE,
    VIDEO,
    MESSAGING,

    /**
     * Default value, could be received if value returned by server is not supported by current version of SDK
     */
    UNKNOWN
}
