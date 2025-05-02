package com.glia.widgets.internal.authentication

import com.glia.androidsdk.visitor.Authentication as CoreAuthentication
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.Dependencies.repositoryFactory
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * Wrapper class for {@link com.glia.androidsdk.visitor.Authentication}
 * Its purpose is to execute Widgets-specific code (e.g. destroy controllers)
 *
 */
internal class AuthenticationManager(
    private val authentication: CoreAuthentication,
    private val onAuthenticationRequestedCallback: () -> Unit
) : Authentication {
    override fun setBehavior(behavior: CoreAuthentication.Behavior) {
        authentication.setBehavior(behavior)
    }

    override fun authenticate(
        jwtToken: String,
        externalAccessToken: String?,
        authCallback: RequestCallback<Void>?
    ) {
        onAuthenticationRequestedCallback()
        Dependencies.destroyControllersAndResetQueueing()

        Logger.i(TAG, "Authenticate. Is external access token used: ${externalAccessToken != null}")
        authentication.authenticate(jwtToken, externalAccessToken) { void, gliaException ->
            if (gliaException == null) {
                //Here we need to subscribe to secure conversations repository to get the data for authenticated visitors
                repositoryFactory.secureConversationsRepository.subscribe()
            }

            authCallback?.onResult(void, gliaException)
        }
    }

    override fun deauthenticate(authCallback: RequestCallback<Void>?) {
        Logger.i(TAG, "Unauthenticate")
        //Need to end engagement before it's done on the core side to prevent unexpected behavior
        Dependencies.destroyControllersAndResetEngagementData()

        //Here we reset the secure conversations repository to clear the data, because the visitor is de-authenticated
        //and we don't need secure conversations data for un-authenticated visitors.
        repositoryFactory.secureConversationsRepository.unsubscribeAndResetData()

        authentication.deauthenticate(authCallback)
    }

    override val isAuthenticated: Boolean
        get() = authentication.isAuthenticated

    override fun refresh(jwtToken: String, externalAccessToken: String?, authCallback: RequestCallback<Void>?) {
        Logger.i(TAG, "Refresh authentication")
        authentication.refresh(jwtToken, externalAccessToken, authCallback)
    }
}

internal fun Authentication.Behavior.toCoreType(): CoreAuthentication.Behavior =
    when (this) {
        Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT -> CoreAuthentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT
        Authentication.Behavior.ALLOWED_DURING_ENGAGEMENT -> CoreAuthentication.Behavior.ALLOWED_DURING_ENGAGEMENT
    }

internal fun CoreAuthentication.Behavior.toWidgetsType(): Authentication.Behavior =
    when (this) {
        CoreAuthentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT -> Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT
        CoreAuthentication.Behavior.ALLOWED_DURING_ENGAGEMENT -> Authentication.Behavior.ALLOWED_DURING_ENGAGEMENT
    }
