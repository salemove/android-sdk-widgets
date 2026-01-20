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

    /**
     * Configuration for the Glia SDK when using a site API key ID and secret
     *
     * @param id     The site API key ID
     * @param secret The site API key secret
     *
     * @see GliaWidgetsConfig
     */
    open class SiteApiKey(val id: String, val secret: String) : AuthorizationMethod
}

internal fun AuthorizationMethod.toCoreType(): com.glia.androidsdk.AuthorizationMethod {
    return when (this) {
        is AuthorizationMethod.SiteApiKey -> com.glia.androidsdk.AuthorizationMethod.SiteApiKey(id, secret)
        is AuthorizationMethod.UserApiKey -> com.glia.androidsdk.AuthorizationMethod.UserApiKey(id, secret)
    }
}

internal fun com.glia.androidsdk.AuthorizationMethod.SiteApiKey.toWidgetType(): AuthorizationMethod.SiteApiKey {
    return AuthorizationMethod.SiteApiKey(id, secret)
}

internal val AuthorizationMethod.apiKeyId: String
    get() = when (this) {
        is AuthorizationMethod.SiteApiKey -> this.id
        is AuthorizationMethod.UserApiKey -> this.id
    }
