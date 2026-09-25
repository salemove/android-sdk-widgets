@file:JvmName("Regions")

package com.glia.widgets

import androidx.core.net.toUri

/**
 * Defines region that can be applied to [GliaWidgetsConfig].
 */
sealed interface Region {
    /**
     * United States region.
     */
    data object US : Region

    /**
     * European region.
     */
    data object EU : Region

    /**
     * Custom region that can accept a domain.
     */
    class Custom(domain: String) : Region {
        val host: String = domain.toUri().host ?: domain

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Custom) return false

            if (host != other.host) return false

            return true
        }

        override fun hashCode(): Int {
            return host.hashCode()
        }

    }
}

/**
 * Beta region. Reserved for Glia's own testing and deliberately kept off the public API —
 * it is reachable only by passing "beta" to [toRegion].
 */
internal data object BetaRegion : Region

internal const val US_REGION = "us"
internal const val EU_REGION = "eu"
internal const val BETA_REGION = "beta"

/**
 * Converts a [String] to a [Region].
 *
 * Supported values are:
 * - "us" for [Region.US]
 * - "eu" for [Region.EU]
 * - any other value will be treated as a custom domain for [Region.Custom]
 *
 * "beta" is reserved for Glia's internal testing.
 */
@JvmName("fromString")
fun String.toRegion(): Region = when (this.lowercase()) {
    US_REGION -> Region.US
    EU_REGION -> Region.EU
    BETA_REGION -> BetaRegion
    else -> Region.Custom(this)
}
