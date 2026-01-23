package com.glia.widgets.base

import com.glia.widgets.locale.LocaleString

/**
 * Contract defining communication between Glia Fragments and their host Activities.
 *
 * Activities that host Glia Fragments should implement [Host] to handle Fragment callbacks.
 */
internal interface GliaFragmentContract {
    /**
     * Interface for host Activities to implement Fragment callbacks.
     */
    interface Host {
        /**
         * Set the Activity title using a localized string.
         *
         * @param locale The localized title, or null to clear the title
         */
        fun setHostTitle(locale: LocaleString?)

        /**
         * Finish the host Activity.
         */
        fun finish()
    }
}
