package com.glia.widgets

/**
 * Configurations used for configuring the Glia SDK with site API Key ID and secret
 *
 * @param id     The site API Key ID
 * @param secret The site API Key secret
 *
 * @see GliaWidgetsConfig
 *
 * @deprecated Will be removed in version 4.0.0.
 */
@Deprecated(
    "Use AuthorizationMethod.SiteApiKey from com.glia.widgets package",
    ReplaceWith(
        "AuthorizationMethod.SiteApiKey(id, secret)",
        "com.glia.widgets.AuthorizationMethod"
    )
)
class SiteApiKey(id: String, secret: String): AuthorizationMethod.SiteApiKey(id, secret)
