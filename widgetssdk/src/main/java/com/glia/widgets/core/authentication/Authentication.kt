package com.glia.widgets.core.authentication

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.visitor.Authentication
import com.glia.widgets.di.Dependencies

/**
 * Wrapper class for {@link com.glia.androidsdk.visitor.Authentication}
 * Its purpose is to execute Widgets-specific code (e.g. destroy controllers)
 *
 */
class Authentication(private val authentication: Authentication) : Authentication {
    override fun setBehavior(behavior: Authentication.Behavior) {
        authentication.setBehavior(behavior)
    }

    override fun authenticate(requestCallback: RequestCallback<Void>, jwtToken: String) {
        Dependencies.getControllerFactory().destroyControllers()
        authentication.authenticate(requestCallback, jwtToken)
    }

    override fun deauthenticate(requestCallback: RequestCallback<Void>) {
        Dependencies.getControllerFactory().destroyControllers()
        authentication.deauthenticate(requestCallback)
    }

    override fun isAuthenticated(): Boolean {
        return authentication.isAuthenticated
    }
}
