package com.glia.widgets

/**
 * Configurations used for configuring the Glia SDK with site API Key ID and secret
 *
 * @param id     The site API Key ID
 * @param secret The site API Key secret
 *
 * @see GliaWidgetsConfig
 */
data class SiteApiKey(val id: String, val secret: String)

internal fun SiteApiKey.toCoreType(): com.glia.androidsdk.SiteApiKey {
    return com.glia.androidsdk.SiteApiKey(id, secret)
}

internal fun com.glia.androidsdk.SiteApiKey.toWidgetType(): SiteApiKey {
    return SiteApiKey(id, secret)
}
