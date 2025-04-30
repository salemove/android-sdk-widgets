package com.glia.widgets.authentication

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.visitor.Authentication

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
     * @throws GliaException [GliaException.Cause.INVALID_INPUT] - in case behavior is null.
     */
    fun setBehavior(behavior: Authentication.Behavior)

    /**
     * Authenticates the visitor.
     *
     * @param jwtToken                JWT token (Direct ID token) for visitor authentication.
     * @param externalAccessToken     An access token that can be used to make authenticated requests
     * to other systems on behalf of the authenticated visitor.
     * @param authCallback callback to receive execution result
     * (null for success or GliaException for error).
     * Exception may have one of following causes:
     * <br></br> [GliaException.Cause.NETWORK_TIMEOUT] - when request times out due to connection issues
     * <br></br> [GliaException.Cause.INTERNAL_ERROR] - when internal error occurs
     * <br></br> [GliaException.Cause.AUTHENTICATION_ERROR] - when authentication fails
     */
    fun authenticate(
        jwtToken: String,
        externalAccessToken: String?,
        authCallback: RequestCallback<Void>?
    )

    /**
     * De-authenticates the visitor.
     *
     * @param authCallback callback to receive execution result
     * (null for success or GliaException for error).
     * Exception may have one of following causes:
     * <br></br> [GliaException.Cause.NETWORK_TIMEOUT] - when request times out due to connection issues
     * <br></br> [GliaException.Cause.INTERNAL_ERROR] - when internal error occurs
     * <br></br> [GliaException.Cause.AUTHENTICATION_ERROR] - when authentication fails
     */
    fun deauthenticate(authCallback: RequestCallback<Void>?)

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
     * @param authCallback callback to receive execution result
     * (null for success or GliaException for error).
     * Exception may have one of following causes:
     * <br></br> [GliaException.Cause.NETWORK_TIMEOUT] - when request times out due to connection issues
     * <br></br> [GliaException.Cause.INTERNAL_ERROR] - when internal error occurs
     * <br></br> [GliaException.Cause.AUTHENTICATION_ERROR] - when authentication fails
     */
    fun refresh(
        jwtToken: String,
        externalAccessToken: String?,
        authCallback: RequestCallback<Void>?
    )

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
