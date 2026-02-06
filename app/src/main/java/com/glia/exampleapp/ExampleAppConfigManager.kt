package com.glia.exampleapp

import android.content.Context
import android.net.Uri
import com.glia.exampleapp.data.model.EnvironmentSelection
import com.glia.exampleapp.data.model.GliaConfiguration
import com.glia.widgets.GliaWidgetsConfig
import com.glia.widgets.SiteApiKey

/**
 * Helper class to create Glia Config from DataStore and parse deep links.
 * All configuration is now stored in DataStore, not SharedPreferences.
 */
object ExampleAppConfigManager {
    // Deep link keys
    private const val SECRET_KEY = "secret"
    private const val SITE_ID_KEY = "site_id"
    private const val API_KEY_SECRET_KEY = "api_key_secret"
    private const val API_KEY_ID_KEY = "api_key_id"
    private const val QUEUE_ID_KEY = "queue_id"
    private const val VISITOR_CONTEXT_ASSET_ID_KEY = "visitor_context_asset_id"
    private const val REGION_KEY = "environment"
    private const val BASE_DOMAIN = "base_domain"
    private const val SUPPRESS_PN_DIALOG_KEY = "suppress_pn_permission_dialog"

    /**
     * Create GliaWidgetsConfig from DataStore-backed GliaConfiguration.
     * Used by the new Compose UI which stores configuration in DataStore.
     */
    @JvmStatic
    fun createConfigFromDataStore(
        context: Context,
        config: GliaConfiguration
    ): GliaWidgetsConfig {
        val region = config.environment.toRegion(config.customEnvironmentUrl)
        val themeJson = config.themeColors.toJsonString()

        return GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey(config.apiKeyId, config.apiKeySecret))
            .setSiteId(config.siteId)
            .setRegion(region)
            .setCompanyName(config.companyName)
            .enableBubbleOutsideApp(config.enableBubbleOutsideApp)
            .enableBubbleInsideApp(config.enableBubbleInsideApp)
            .setContext(context)
            .setUiJsonRemoteConfig(themeJson)
            .setManualLocaleOverride(config.manualLocaleOverride.takeIf { it.isNotBlank() })
            .setSuppressPushNotificationsPermissionRequestDuringAuthentication(config.suppressPushNotificationDialog)
            .build()
    }

    /**
     * Parse deep link URI and merge with existing configuration.
     * Only updates fields that are present in the deep link URL.
     * Returns null if the deep link format is invalid.
     */
    @JvmStatic
    fun parseDeepLinkToConfiguration(uri: Uri, context: Context, existingConfig: GliaConfiguration): GliaConfiguration? {
        if (SECRET_KEY != uri.lastPathSegment) {
            return null
        }

        // Extract values from deep link (only if present)
        val siteId = uri.getQueryParameter(SITE_ID_KEY)
        val apiKeyId = uri.getQueryParameter(API_KEY_ID_KEY)
        val apiKeySecret = uri.getQueryParameter(API_KEY_SECRET_KEY)
        val queueId = uri.getQueryParameter(QUEUE_ID_KEY)
        val visitorContextAssetId = uri.getQueryParameter(VISITOR_CONTEXT_ASSET_ID_KEY)
        val suppressPnDialog = uri.getQueryParameter(SUPPRESS_PN_DIALOG_KEY)

        // Determine environment (only if present)
        val baseDomain = uri.getQueryParameter(BASE_DOMAIN)
        val regionStr = uri.getQueryParameter(REGION_KEY)

        val environment = when {
            baseDomain != null -> EnvironmentSelection.CUSTOM
            regionStr != null -> EnvironmentSelection.fromString(regionStr)
            else -> existingConfig.environment
        }

        // Merge with existing configuration - deep link values override existing
        return existingConfig.copy(
            siteId = siteId ?: existingConfig.siteId,
            apiKeyId = apiKeyId ?: existingConfig.apiKeyId,
            apiKeySecret = apiKeySecret ?: existingConfig.apiKeySecret,
            environment = environment,
            customEnvironmentUrl = baseDomain ?: existingConfig.customEnvironmentUrl,
            queueId = queueId ?: existingConfig.queueId,
            visitorContextAssetId = visitorContextAssetId ?: existingConfig.visitorContextAssetId,
            suppressPushNotificationDialog = suppressPnDialog?.toBoolean() ?: existingConfig.suppressPushNotificationDialog
        )
    }
}
