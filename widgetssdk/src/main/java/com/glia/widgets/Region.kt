package com.glia.widgets

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

    // -- Internal use only

    /**
     * Beta region for testing purposes.
     */
    data object Beta : Region

    /**
     * Custom region that can accept a domain.
     */
    data class Custom(val domain: String) : Region
}
