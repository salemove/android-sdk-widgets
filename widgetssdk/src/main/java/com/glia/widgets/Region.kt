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
