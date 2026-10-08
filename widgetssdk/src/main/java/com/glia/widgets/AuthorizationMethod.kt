package com.glia.widgets

/**
 * Defines the authorization method used to configure the Glia SDK.
 *
 * @see GliaWidgetsConfig
 */
sealed interface AuthorizationMethod {

    /**
     * Configuration for the Glia SDK when using a (service credential's) user API key ID and secret
     *
     * @param id     The user API key ID
     * @param secret The user API key secret
     *
     * @see GliaWidgetsConfig
     */
    data class UserApiKey(val id: String, val secret: String) : AuthorizationMethod
}

internal fun AuthorizationMethod.toCoreType(): com.glia.androidsdk.AuthorizationMethod {
    return when (this) {
        is AuthorizationMethod.UserApiKey -> com.glia.androidsdk.AuthorizationMethod.UserApiKey(id, secret)
    }
}

internal val AuthorizationMethod.apiKeyId: String
    get() = when (this) {
        is AuthorizationMethod.UserApiKey -> this.id
    }
