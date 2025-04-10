package com.glia.widgets.core.visitor

import com.glia.androidsdk.visitor.Authentication as CoreAuthentication

/**
 * Interface for managing authentication and de-authentication.
 *
 * The `Behavior` enum defines behavior for authentication and de-authentication
 * in different scenarios.
 */
interface Authentication: CoreAuthentication {
    /**
     * Behavior for authentication and de-authentication.
     */
    enum class Behavior {
        /**
         * Forbid authentication and de-authentication during ongoing engagement.
         */
        FORBIDDEN_DURING_ENGAGEMENT,

        /**
         * Allow authentication and de-authentication during ongoing engagement.
         */
        ALLOWED_DURING_ENGAGEMENT;
    }
}
