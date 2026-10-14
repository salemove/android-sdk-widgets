package com.glia.widgets.authentication

import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.callbacks.OnComplete
import com.glia.widgets.callbacks.OnError

/**
 * Interface for managing authentication and de-authentication.
 *
 * The `Behavior` enum defines behavior for authentication and de-authentication
 * in different scenarios.
 */
interface Authentication {
    /**
     * Sets the specified behavior.
     *
     * @param behavior authentication behavior
     * @throws GliaWidgetsException [GliaWidgetsException.Cause.INVALID_INPUT] - in case behavior is null.
     */
    fun setBehavior(behavior: Behavior)

    /**
     * Authenticates the visitor.
     *
     * @param jwtToken                JWT token (Direct ID token) for visitor authentication.
     * @param externalAccessToken     An access token that can be used to make authenticated requests
     * to other systems on behalf of the authenticated visitor.
     * @param onComplete Callback invoked when the update operation is successfully completed.
     * @param onError Callback invoked when an error occurs during the update operation.
     *                Provides a [GliaWidgetsException] describing the error.
     * Exception may have one of following causes:
     * <br></br> [GliaWidgetsException.Cause.NETWORK_TIMEOUT] - when request times out due to connection issues
     * <br></br> [GliaWidgetsException.Cause.INTERNAL_ERROR] - when internal error occurs
     * <br></br> [GliaWidgetsException.Cause.AUTHENTICATION_ERROR] - when authentication fails
     */
    fun authenticate(
        jwtToken: String,
        externalAccessToken: String?,
        onComplete: OnComplete,
        onError: OnError
    )

    /**
     * De-authenticates the visitor.
     *
     * @param stopPushNotifications whether to unsubscribe from receiving push notifications for the de-authenticated visitor
     * (null for success or GliaException for error).
     * @param onComplete Callback invoked when the update operation is successfully completed.
     * @param onError Callback invoked when an error occurs during the update operation.
     *                Provides a [GliaWidgetsException] describing the error.
     * Exception may have one of following causes:
     * <br></br> [GliaWidgetsException.Cause.NETWORK_TIMEOUT] - when request times out due to connection issues
     * <br></br> [GliaWidgetsException.Cause.INTERNAL_ERROR] - when internal error occurs
     * <br></br> [GliaWidgetsException.Cause.AUTHENTICATION_ERROR] - when authentication fails
     */
    fun deauthenticate(
        stopPushNotifications: Boolean,
        onComplete: OnComplete,
        onError: OnError
    )

    /**
     * Same as [deauthenticate] but with default value `false` for `stopPushNotifications` parameter.
     */
    fun deauthenticate(
        onComplete: OnComplete,
        onError: OnError
    ) {
        deauthenticate(false, onComplete, onError)
    }

    /**
     * Check if Visitor is authenticated in Glia using the external authentication.
     *
     * @return `true` if Visitor is authenticated in Glia using the external authentication,
     * `false` otherwise.
     */
    val isAuthenticated: Boolean

    /**
     * Refresh visitor id token.
     *
     * @param jwtToken                JWT token (Direct ID token) for visitor authentication.
     * @param externalAccessToken     An access token that can be used to make authenticated requests
     * to other systems on behalf of the authenticated visitor.
     * @param onComplete Callback invoked when the update operation is successfully completed.
     * @param onError Callback invoked when an error occurs during the update operation.
     *                Provides a [GliaWidgetsException] describing the error.
     * Exception may have one of following causes:
     * <br></br> [GliaWidgetsException.Cause.NETWORK_TIMEOUT] - when request times out due to connection issues
     * <br></br> [GliaWidgetsException.Cause.INTERNAL_ERROR] - when internal error occurs
     * <br></br> [GliaWidgetsException.Cause.AUTHENTICATION_ERROR] - when authentication fails
     */
    fun refresh(
        jwtToken: String,
        externalAccessToken: String?,
        onComplete: OnComplete,
        onError: OnError
    )

    /**
     * Sets the listener called when the backend refuses the JWT the visitor is authenticated with,
     * which happens once it expired. The visitor stays authenticated and keeps its engagement, but its
     * requests fail until [refresh] succeeds with a new JWT.
     *
     * Called once per expired JWT, on the main thread.
     *
     * @param listener the listener, or null to remove the current one.
     */
    fun setTokenExpiredListener(listener: TokenExpiredListener?)

    /**
     * See [setTokenExpiredListener].
     */
    fun interface TokenExpiredListener {
        /**
         * Called when the JWT the visitor is authenticated with expired. Call [refresh] with a new JWT.
         */
        fun onTokenExpired()
    }

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
