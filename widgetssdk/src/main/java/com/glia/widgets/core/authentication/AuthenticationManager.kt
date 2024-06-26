package com.glia.widgets.core.authentication

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.visitor.Authentication
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * Wrapper class for {@link com.glia.androidsdk.visitor.Authentication}
 * Its purpose is to execute Widgets-specific code (e.g. destroy controllers)
 *
 */
internal class AuthenticationManager(
    private val authentication: Authentication
) : Authentication {
    override fun setBehavior(behavior: Authentication.Behavior) {
        authentication.setBehavior(behavior)
    }

    @Deprecated(
        "Please use authenticate(String, String?, RequestCallback<Void>)",
        ReplaceWith("authenticate(jwtToken, null, requestCallback)")
    )
    override fun authenticate(requestCallback: RequestCallback<Void>, jwtToken: String) {
        Logger.logDeprecatedMethodUse(TAG, "authenticate(RequestCallback<Void>, String)")
        cleanup()
        authentication.authenticate(jwtToken, null, requestCallback)
    }

    override fun authenticate(
        jwtToken: String,
        externalAccessToken: String?,
        requestCallback: RequestCallback<Void>
    ) {
        cleanup()
        Logger.i(TAG, "Authenticate. Is external access token used: ${externalAccessToken != null}")
        authentication.authenticate(jwtToken, externalAccessToken, requestCallback)
    }

    override fun deauthenticate(requestCallback: RequestCallback<Void>) {
        Logger.i(TAG, "Unauthenticate")
        cleanup()
        authentication.deauthenticate(requestCallback)
    }

    private fun cleanup() {
        Dependencies.destroyControllersAndResetQueueing()
    }

    override fun isAuthenticated(): Boolean = authentication.isAuthenticated

    override fun refresh(jwtToken: String?, externalAccessToken: String?, authCallback: RequestCallback<Void>?) {
        Logger.i(TAG, "Refresh authentication")
        authentication.refresh(jwtToken, externalAccessToken, authCallback)
    }
}
