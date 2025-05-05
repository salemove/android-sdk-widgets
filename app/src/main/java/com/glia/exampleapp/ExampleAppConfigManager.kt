package com.glia.exampleapp

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.glia.androidsdk.SiteApiKey
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.GliaWidgetsConfig
import androidx.core.content.edit

/**
 * Helper class to obtain Glia Config params from deep-link or preferences.
 *
 * for setup with a secret link must be
 * glia://widgets/secret?site_id={site_id}&api_key_secret={api_key_secret}&api_key_id={api_key_id}&queue_id={queue_id}&visitor_context_asset_id={visitor_context_asset_id}
 * where all query params are mandatory except visitor_context_asset_id
 */
object ExampleAppConfigManager {
    private const val SECRET_KEY = "secret"
    private const val SITE_ID_KEY = "site_id"
    private const val API_KEY_SECRET_KEY = "api_key_secret"
    private const val API_KEY_ID_KEY = "api_key_id"
    private const val QUEUE_ID_KEY = "queue_id"
    private const val VISITOR_CONTEXT_ASSET_ID_KEY = "visitor_context_asset_id"
    private const val REGION_KEY = "environment"
    private const val REGION_ACCEPTANCE = "acceptance"
    private const val BASE_DOMAIN = "base_domain"
    private const val DEFAULT_BASE_DOMAIN = "at.samo.io"
    private const val SUPPRESS_PN_DIALOG_KEY = "suppress_pn_permission_dialog"

    @JvmStatic
    fun obtainConfigFromDeepLink(data: Uri, applicationContext: Context): GliaWidgetsConfig {
        saveRegionIfPresent(data, applicationContext)
        saveQueueIdToPrefs(data, applicationContext)
        saveVisitorContextAssetIdIfPresent(data, applicationContext)
        saveSiteIdToPrefs(data, applicationContext)
        saveSuppressPushNotificationDialogToPrefs(data, applicationContext)

//       We're not checking availability for every query param separately because this is for acceptance tests and we assume that link would be correct
        if (SECRET_KEY == data.lastPathSegment) {
            saveSiteApiKeyAuthToPrefs(data, applicationContext)
        } else {
            throw RuntimeException("deep link must start with \"glia://widgets/secret\"")
        }
        val region = data.getQueryParameter(REGION_KEY) ?: REGION_ACCEPTANCE
        val baseDomain = data.getQueryParameter(BASE_DOMAIN) ?: DEFAULT_BASE_DOMAIN

        // By this point all settings from deep link where saved to the shared prefs overriding the
        // defaults combining both together
        return createDefaultConfig(
            context = applicationContext,
            region = region,
            baseDomain = baseDomain
        )
    }

    private fun saveRegionIfPresent(data: Uri, applicationContext: Context) {
        val visitorContextAssetId = data.getQueryParameter(REGION_KEY) ?: return
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit() {
            putString(
                applicationContext.getString(R.string.pref_environment),
                visitorContextAssetId
            )
        }
    }

    private fun saveVisitorContextAssetIdIfPresent(data: Uri, applicationContext: Context) {
        val visitorContextAssetId = data.getQueryParameter(VISITOR_CONTEXT_ASSET_ID_KEY) ?: return
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit() {
            putString(
                applicationContext.getString(R.string.pref_context_asset_id),
                visitorContextAssetId
            )
        }
    }

    private fun saveQueueIdToPrefs(data: Uri, applicationContext: Context) {
        val queueId = data.getQueryParameter(QUEUE_ID_KEY) ?: return
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit() {
            putString(applicationContext.getString(R.string.pref_queue_id), queueId)
        }
    }

    private fun saveSiteIdToPrefs(data: Uri, applicationContext: Context) {
        val siteId = data.getQueryParameter(SITE_ID_KEY) ?: return
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit() {
            putString(applicationContext.getString(R.string.pref_site_id), siteId)
        }
    }

    private fun saveSuppressPushNotificationDialogToPrefs(data: Uri, applicationContext: Context) {
        val suppressPnDialog = data.getQueryParameter(SUPPRESS_PN_DIALOG_KEY)?.toBooleanStrictOrNull() ?: return
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit() {
            putBoolean(applicationContext.getString(R.string.pref_suppress_p_n_during_auth), suppressPnDialog)
        }
    }

    private fun saveSiteApiKeyAuthToPrefs(data: Uri, applicationContext: Context) {
        val apiKeyId = data.getQueryParameter(API_KEY_ID_KEY)
        val apiKeySecret = data.getQueryParameter(API_KEY_SECRET_KEY)
        if (apiKeyId == null || apiKeySecret == null) return
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit() {
            putString(applicationContext.getString(R.string.pref_api_key_id), apiKeyId)
                .putString(applicationContext.getString(R.string.pref_api_key_secret), apiKeySecret)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun createDefaultConfig(
        context: Context,
        uiJsonRemoteConfig: String? = null,
        region: String? = null,
        baseDomain: String = DEFAULT_BASE_DOMAIN,
        preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ): GliaWidgetsConfig {
        val siteRegion = region ?: preferences.getString(
            context.getString(R.string.pref_environment),
            context.getString(R.string.environment)
        )
        val apiKeyId = preferences.getString(
            context.getString(R.string.pref_api_key_id),
            context.getString(R.string.glia_api_key_id)
        )
        val apiKeySecret = preferences.getString(
            context.getString(R.string.pref_api_key_secret),
            context.getString(R.string.glia_api_key_secret)
        )
        val siteId = preferences.getString(
            context.getString(R.string.pref_site_id),
            context.getString(R.string.site_id)
        )
        val companyName = preferences.getString(
            context.getString(R.string.pref_company_name),
            context.getString(R.string.settings_value_default_company_name)
        )
        val enableBubbleOutsideApp = preferences.getBoolean(
            context.getString(R.string.settings_enable_bubble_outside_app),
            true
        )
        val enableBubbleInsideApp = preferences.getBoolean(
            context.getString(R.string.settings_enable_bubble_inside_app),
            true
        )
        val suppressPushNotificationDialogDuringAuthentication = preferences.getBoolean(
            context.getString(R.string.pref_suppress_p_n_during_auth),
            false
        )
        val bounded = context.getString(R.string.screen_sharing_mode_app_bounded)
        val unbounded = context.getString(R.string.screen_sharing_mode_unbounded)
        val screenSharingMode = if (
            preferences.getString(context.getString(R.string.pref_screen_sharing_mode), bounded) == bounded
        ) {
            ScreenSharing.Mode.APP_BOUNDED
        } else {
            ScreenSharing.Mode.UNBOUNDED
        }
        val manualLocaleOverride = preferences.getString(context.getString(R.string.pref_manual_locale_override), null)
        return GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey(apiKeyId!!, apiKeySecret!!))
            .setSiteId(siteId)
            .setRegion(siteRegion)
            .setBaseDomain(baseDomain)
            .setCompanyName(companyName)
            .enableBubbleOutsideApp(enableBubbleOutsideApp)
            .enableBubbleInsideApp(enableBubbleInsideApp)
            .setScreenSharingMode(screenSharingMode)
            .setContext(context)
            .setUiJsonRemoteConfig(uiJsonRemoteConfig ?: Utils.getRemoteThemeByPrefs(preferences, context.resources))
            .setManualLocaleOverride(manualLocaleOverride)
            .setSuppressPushNotificationsPermissionRequestDuringAuthentication(suppressPushNotificationDialogDuringAuthentication)
            .build()
    }
}
