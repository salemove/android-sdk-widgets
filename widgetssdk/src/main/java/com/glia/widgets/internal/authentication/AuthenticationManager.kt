package com.glia.widgets.internal.authentication

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.visitor.Authentication as CoreAuthentication
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callbacks.toOnComplete
import com.glia.widgets.callbacks.toOnError
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.Dependencies.repositoryFactory
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.toWidgetsType

/**
 * Wrapper class for {@link com.glia.androidsdk.visitor.Authentication}
 * Its purpose is to execute Widgets-specific code (e.g. destroy controllers)
 *
 */
internal class AuthenticationManager(
    private val authentication: CoreAuthentication,
    private val onAuthenticationRequestedCallback: () -> Unit
) : Authentication {

    override val isAuthenticated: Boolean
        get() = authentication.isAuthenticated

    override fun setBehavior(behavior: Authentication.Behavior) {
        authentication.setBehavior(behavior.toCoreType())
    }

    override fun authenticate(
        jwtToken: String,
        externalAccessToken: String?,
        onComplete: OnComplete,
        onError: OnError
    ) {
        onAuthenticationRequestedCallback()
        Dependencies.destroyControllersAndResetQueueing()

        Logger.i(TAG, "Authenticate. Is external access token used: ${externalAccessToken != null}")
        authentication.authenticate(jwtToken, externalAccessToken) { _, gliaException ->
            if (gliaException != null) {
                onError.onError(gliaException.toWidgetsType())
            } else {
                //Here we need to subscribe to secure conversations repository to get the data for authenticated visitors
                repositoryFactory.secureConversationsRepository.subscribe()
                onComplete.onComplete()
                Dependencies.controllerFactory.pushClickHandlerController.onAuthenticated()
            }
        }
    }

    override fun deauthenticate(
        stopPushNotifications: Boolean,
        onComplete: OnComplete,
        onError: OnError
    ) {
        Logger.i(TAG, "Unauthenticate")
        //Need to end engagement before it's done on the core side to prevent unexpected behavior
        Dependencies.destroyControllersAndResetEngagementData()

        //Here we reset the secure conversations repository to clear the data, because the visitor is de-authenticated
        //and we don't need secure conversations data for un-authenticated visitors.
        repositoryFactory.secureConversationsRepository.unsubscribeAndResetData()

        authentication.deauthenticate(stopPushNotifications) { _, gliaException ->
            if (gliaException != null) {
                onError.onError(gliaException.toWidgetsType())
            } else {
                onComplete.onComplete()
            }
        }
    }

    override fun refresh(jwtToken: String,
                         externalAccessToken: String?,
                         onComplete: OnComplete,
                         onError: OnError
    ) {
        Logger.i(TAG, "Refresh authentication")
        authentication.refresh(jwtToken, externalAccessToken) { _, gliaException ->
            if (gliaException != null) {
                onError.onError(gliaException.toWidgetsType())
            } else {
                onComplete.onComplete()
            }
        }
    }
}

internal fun AuthenticationManager.toCoreType(): CoreAuthentication = this.let { widgetAuthentication ->
    object : CoreAuthentication {
        override fun setBehavior(behavior: CoreAuthentication.Behavior) {
            widgetAuthentication.setBehavior(behavior.toWidgetsType())
        }

        override fun authenticate(jwtToken: String, externalAccessToken: String?, authCallback: RequestCallback<Void>?) {
            if (jwtToken.isBlank()) {
                reportTokenInvalidError(authCallback)
                return
            }
            widgetAuthentication.authenticate(jwtToken, externalAccessToken, authCallback.toOnComplete(), authCallback.toOnError())
        }

        override fun deauthenticate(stopPushNotifications: Boolean, authCallback: RequestCallback<Void>?) {
            widgetAuthentication.deauthenticate(authCallback.toOnComplete(), authCallback.toOnError())
        }

        override fun deauthenticate(authCallback: RequestCallback<Void>?) {
            widgetAuthentication.deauthenticate(authCallback.toOnComplete(), authCallback.toOnError())
        }

        override val isAuthenticated: Boolean
            get() = widgetAuthentication.isAuthenticated

        override fun refresh(jwtToken: String, externalAccessToken: String?, authCallback: RequestCallback<Void>?) {
            if (jwtToken.isBlank()) {
                reportTokenInvalidError(authCallback)
                return
            }
            widgetAuthentication.refresh(jwtToken, externalAccessToken, authCallback.toOnComplete(), authCallback.toOnError())
        }

        private fun reportTokenInvalidError(authCallback: RequestCallback<Void>?) {
            val errorMessage = "JWT token is not valid or empty"
            val invalidInputException = GliaException(errorMessage, GliaException.Cause.INVALID_INPUT)
            authCallback?.onResult(null, invalidInputException)
        }
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
