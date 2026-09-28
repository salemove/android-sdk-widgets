package com.glia.widgets.internal.authentication

import com.glia.androidsdk.GliaException
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.SdkType
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.Dependencies.repositoryFactory
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.toCoreType
import com.glia.widgets.toWidgetsType
import com.glia.androidsdk.visitor.Authentication as CoreAuthentication

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
        GliaLogger.logMethodUse(Authentication::class, "setBehavior", "behavior")
        authentication.setBehavior(behavior.toCoreType())
    }

    override fun authenticate(jwtToken: String, externalAccessToken: String?, onComplete: OnComplete, onError: OnError) {
        GliaLogger.logMethodUse(Authentication::class, "authenticate")

        onAuthenticationRequestedCallback()
        Dependencies.destroyControllersAndResetQueueing()

        Logger.i(TAG, "Authenticate. Is external access token used: ${externalAccessToken != null}")
        //onAuthenticationAttempt must be called inside both authentication callbacks, because we need to handle failed authentication as well
        authentication.authenticate(jwtToken, externalAccessToken, {
            //Here we need to subscribe to secure conversations repository to get the data for authenticated visitors
            repositoryFactory.secureConversationsRepository.subscribe()
            onComplete.onComplete()
            Dependencies.controllerFactory.pushClickHandlerController.onAuthenticationAttempt()
        }, { gliaException ->
            onError.onError(gliaException.toWidgetsType())
            Dependencies.controllerFactory.pushClickHandlerController.onAuthenticationAttempt()
        })
    }

    override fun deauthenticate(stopPushNotifications: Boolean, onComplete: OnComplete, onError: OnError) {
        GliaLogger.logMethodUse(Authentication::class, "deauthenticate")
        Logger.i(TAG, "Unauthenticate")

        //Need to cancel queueing before de-authentication, because it uses current visitor id, so after de-authentication will be impossible.
        repositoryFactory.engagementRepository.cancelQueuing()

        authentication.deauthenticate(stopPushNotifications, {
            //Reset controllers and data on success block, to keep current engagement interactive in case de-authentication is forbidden during engagement
            Dependencies.destroyControllersAndResetEngagementData()

            //Here we reset the secure conversations repository to clear the data, because the visitor is de-authenticated
            //and we don't need secure conversations data for un-authenticated visitors.
            repositoryFactory.secureConversationsRepository.unsubscribeAndResetData()

            onComplete.onComplete()
        }, { gliaException ->
            onError.onError(gliaException.toWidgetsType())
        })
    }

    override fun refresh(jwtToken: String, externalAccessToken: String?, onComplete: OnComplete, onError: OnError) {
        GliaLogger.logMethodUse(Authentication::class, "refresh")
        Logger.i(TAG, "Refresh authentication")
        authentication.refresh(jwtToken, externalAccessToken, {
            onComplete.onComplete()
        }, { gliaException ->
            onError.onError(gliaException.toWidgetsType())
        })
    }
}

internal fun AuthenticationManager.toCoreType(): CoreAuthentication = this.let { widgetAuthentication ->
    object : CoreAuthentication {
        override fun setBehavior(behavior: CoreAuthentication.Behavior) {
            GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "setBehavior", "behavior")
            widgetAuthentication.setBehavior(behavior.toWidgetsType())
        }

        override fun authenticate(jwtToken: String, externalAccessToken: String?, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
            GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "authenticate")
            if (jwtToken.isBlank()) {
                reportTokenInvalidError(onFailure)
                return
            }
            widgetAuthentication.authenticate(jwtToken, externalAccessToken, { onSuccess() }, { onFailure(it.toCoreType()) })
        }

        override fun deauthenticate(stopPushNotifications: Boolean, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
            GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "deauthenticate", "stopPushNotifications", "callback")
            widgetAuthentication.deauthenticate(stopPushNotifications, { onSuccess() }, { onFailure(it.toCoreType()) })
        }

        override fun deauthenticate(onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
            GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "deauthenticate", "callback")
            widgetAuthentication.deauthenticate({ onSuccess() }, { onFailure(it.toCoreType()) })
        }

        override val isAuthenticated: Boolean
            get() {
                GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "isAuthenticated")
                return widgetAuthentication.isAuthenticated
            }

        override fun refresh(jwtToken: String, externalAccessToken: String?, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
            GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "refresh")
            if (jwtToken.isBlank()) {
                reportTokenInvalidError(onFailure)
                return
            }
            widgetAuthentication.refresh(jwtToken, externalAccessToken, { onSuccess() }, { onFailure(it.toCoreType()) })
        }

        private fun reportTokenInvalidError(onFailure: (GliaException) -> Unit) {
            val errorMessage = "JWT token is not valid or empty"
            val invalidInputException = GliaException(errorMessage, GliaException.Cause.INVALID_INPUT)
            onFailure(invalidInputException)
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
