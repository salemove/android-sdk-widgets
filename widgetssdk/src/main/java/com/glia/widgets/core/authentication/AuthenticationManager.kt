package com.glia.widgets.core.authentication

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.visitor.Authentication
import com.glia.widgets.core.engagement.domain.ResetSurveyUseCase
import com.glia.widgets.di.ControllerFactory

/**
 * Wrapper class for {@link com.glia.androidsdk.visitor.Authentication}
 * Its purpose is to execute Widgets-specific code (e.g. destroy controllers)
 *
 */
internal class AuthenticationManager(
    private val authentication: Authentication,
    private val resetSurveyUseCase: ResetSurveyUseCase,
    private val controllerFactory: ControllerFactory
) : Authentication {
    override fun setBehavior(behavior: Authentication.Behavior) {
        authentication.setBehavior(behavior)
    }

    @Deprecated(
        "Please use authenticate(String, String?, RequestCallback<Void>)",
        ReplaceWith("authenticate(jwtToken, null, requestCallback)")
    )
    override fun authenticate(requestCallback: RequestCallback<Void>, jwtToken: String) {
        cleanup()
        authentication.authenticate(jwtToken, null, requestCallback)
    }

    override fun authenticate(
        jwtToken: String,
        externalAccessToken: String?,
        requestCallback: RequestCallback<Void>
    ) {
        cleanup()
        authentication.authenticate(jwtToken, externalAccessToken, requestCallback)
    }

    override fun deauthenticate(requestCallback: RequestCallback<Void>) {
        cleanup()
        authentication.deauthenticate(requestCallback)
    }

    private fun cleanup() {
        controllerFactory.destroyControllers()
        resetSurveyUseCase()
    }

    override fun isAuthenticated(): Boolean = authentication.isAuthenticated
}
