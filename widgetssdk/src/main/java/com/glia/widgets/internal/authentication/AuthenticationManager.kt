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
        authentication.authenticate(
            jwtToken,
            externalAccessToken,
            onSuccess = {
                //Here we need to subscribe to secure conversations repository to get the data for authenticated visitors
                repositoryFactory.secureConversationsRepository.subscribe()
                onComplete.onComplete()
                reportAuthenticationAttempt()
            },
            onFailure = {
                onError.onError(it.toWidgetsType())
                reportAuthenticationAttempt()
            }
        )
    }

    override fun deauthenticate(stopPushNotifications: Boolean, onComplete: OnComplete, onError: OnError) {
        GliaLogger.logMethodUse(Authentication::class, "deauthenticate")
        Logger.i(TAG, "Unauthenticate")

        //Need to cancel queueing before de-authentication, because it uses current visitor id, so after de-authentication will be impossible.
        repositoryFactory.engagementRepository.cancelQueuing()

        authentication.deauthenticate(
            stopPushNotifications,
            onSuccess = {
                //Reset controllers and data on success block, to keep current engagement interactive in case de-authentication is forbidden during engagement
                Dependencies.destroyControllersAndResetEngagementData()

                //Here we reset the secure conversations repository to clear the data, because the visitor is de-authenticated
                //and we don't need secure conversations data for un-authenticated visitors.
                repositoryFactory.secureConversationsRepository.unsubscribeAndResetData()

                onComplete.onComplete()
            },
            onFailure = { onError.onError(it.toWidgetsType()) }
        )
    }

    override fun refresh(jwtToken: String, externalAccessToken: String?, onComplete: OnComplete, onError: OnError) {
        GliaLogger.logMethodUse(Authentication::class, "refresh")
        Logger.i(TAG, "Refresh authentication")
        authentication.refresh(
            jwtToken,
            externalAccessToken,
            onSuccess = onComplete::onComplete,
            onFailure = { onError.onError(it.toWidgetsType()) }
        )
    }

    //A failed authentication must be reported as well, so every outcome has to report the attempt.
    private fun reportAuthenticationAttempt() {
        Dependencies.controllerFactory.pushClickHandlerController.onAuthenticationAttempt()
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
            widgetAuthentication.authenticate(jwtToken, externalAccessToken, onSuccess.toOnComplete(), onFailure.toOnError())
        }

        override fun deauthenticate(stopPushNotifications: Boolean, onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
            GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "deauthenticate", "stopPushNotifications", "callback")
            widgetAuthentication.deauthenticate(stopPushNotifications, onSuccess.toOnComplete(), onFailure.toOnError())
        }

        override fun deauthenticate(onSuccess: () -> Unit, onFailure: (GliaException) -> Unit) {
            GliaLogger.logDeprecatedApiUse(SdkType.WIDGETS_SDK, CoreAuthentication::class, "deauthenticate", "callback")
            widgetAuthentication.deauthenticate(onSuccess.toOnComplete(), onFailure.toOnError())
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
            widgetAuthentication.refresh(jwtToken, externalAccessToken, onSuccess.toOnComplete(), onFailure.toOnError())
        }

        private fun reportTokenInvalidError(onFailure: (GliaException) -> Unit) {
            val errorMessage = "JWT token is not valid or empty"
            onFailure(GliaException(errorMessage, GliaException.Cause.INVALID_INPUT))
        }
    }
}

private fun (() -> Unit).toOnComplete(): OnComplete = OnComplete { this() }

private fun ((GliaException) -> Unit).toOnError(): OnError = OnError { this(it.toCoreType()) }

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
