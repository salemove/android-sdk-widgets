package com.glia.widgets

/**
 * Configurations used for configuring the Glia SDK with site API Key ID and secret
 *
 * @param id     The site API Key ID
 * @param secret The site API Key secret
 *
 * @see GliaWidgetsConfig
 */
class SiteApiKey(id: String, secret: String): AuthorizationMethod.SiteApiKey(id, secret)
