package com.glia.exampleapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.glia.exampleapp.R
import com.glia.exampleapp.data.model.EnvironmentSelection
import com.glia.exampleapp.data.model.GliaConfiguration
import com.glia.exampleapp.data.model.PredefinedColor
import com.glia.exampleapp.data.model.ThemeColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "glia_configuration")

class ConfigurationRepository(private val context: Context) {

    private object Keys {
        // Site Configuration
        val SITE_ID = stringPreferencesKey("site_id")
        val API_KEY_ID = stringPreferencesKey("api_key_id")
        val API_KEY_SECRET = stringPreferencesKey("api_key_secret")
        val ENVIRONMENT = stringPreferencesKey("environment")
        val CUSTOM_ENVIRONMENT_URL = stringPreferencesKey("custom_environment_url")

        // Engagement Settings
        val QUEUE_ID = stringPreferencesKey("queue_id")
        val VISITOR_CONTEXT_ASSET_ID = stringPreferencesKey("visitor_context_asset_id")
        val USE_DEFAULT_QUEUES = booleanPreferencesKey("use_default_queues")

        // Authentication
        val SAVED_AUTH_TOKEN = stringPreferencesKey("saved_auth_token")

        // Company Settings
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val MANUAL_LOCALE_OVERRIDE = stringPreferencesKey("manual_locale_override")

        // Bubble Settings
        val ENABLE_BUBBLE_OUTSIDE_APP = booleanPreferencesKey("enable_bubble_outside_app")
        val ENABLE_BUBBLE_INSIDE_APP = booleanPreferencesKey("enable_bubble_inside_app")

        // Authentication Settings
        val SUPPRESS_PUSH_NOTIFICATION_DIALOG = booleanPreferencesKey("suppress_push_notification_dialog")
        val AUTO_CONFIGURE_ENABLED = booleanPreferencesKey("auto_configure_enabled")
        val AUTHENTICATION_BEHAVIOR_ALLOWED = booleanPreferencesKey("authentication_behavior_allowed")

        // Theme Settings
        val REMOTE_THEME_ENABLED = booleanPreferencesKey("remote_theme_enabled")
        val PRIMARY_COLOR = stringPreferencesKey("primary_color")
        val SECONDARY_COLOR = stringPreferencesKey("secondary_color")
        val BASE_NORMAL_COLOR = stringPreferencesKey("base_normal_color")
        val BASE_LIGHT_COLOR = stringPreferencesKey("base_light_color")
        val BASE_DARK_COLOR = stringPreferencesKey("base_dark_color")
        val BASE_SHADE_COLOR = stringPreferencesKey("base_shade_color")
        val BACKGROUND_COLOR = stringPreferencesKey("background_color")
        val SYSTEM_NEGATIVE_COLOR = stringPreferencesKey("system_negative_color")
    }

    val configuration: Flow<GliaConfiguration> = context.dataStore.data.map { preferences ->
        // Use DataStore values if present, otherwise fall back to string resources from local.properties
        GliaConfiguration(
            siteId = preferences[Keys.SITE_ID] ?: context.getString(R.string.site_id),
            apiKeyId = preferences[Keys.API_KEY_ID] ?: context.getString(R.string.glia_api_key_id),
            apiKeySecret = preferences[Keys.API_KEY_SECRET] ?: context.getString(R.string.glia_api_key_secret),
            environment = EnvironmentSelection.fromString(preferences[Keys.ENVIRONMENT] ?: context.getString(R.string.glia_region)),
            customEnvironmentUrl = preferences[Keys.CUSTOM_ENVIRONMENT_URL] ?: "",
            queueId = preferences[Keys.QUEUE_ID] ?: context.getString(R.string.glia_queue_id),
            visitorContextAssetId = preferences[Keys.VISITOR_CONTEXT_ASSET_ID] ?: "",
            useDefaultQueues = preferences[Keys.USE_DEFAULT_QUEUES] ?: false,
            savedAuthToken = preferences[Keys.SAVED_AUTH_TOKEN] ?: "",
            companyName = preferences[Keys.COMPANY_NAME] ?: context.getString(R.string.settings_value_default_company_name),
            manualLocaleOverride = preferences[Keys.MANUAL_LOCALE_OVERRIDE] ?: "",
            enableBubbleOutsideApp = preferences[Keys.ENABLE_BUBBLE_OUTSIDE_APP] ?: true,
            enableBubbleInsideApp = preferences[Keys.ENABLE_BUBBLE_INSIDE_APP] ?: true,
            suppressPushNotificationDialog = preferences[Keys.SUPPRESS_PUSH_NOTIFICATION_DIALOG] ?: false,
            autoConfigureEnabled = preferences[Keys.AUTO_CONFIGURE_ENABLED] ?: true,
            authenticationBehaviorAllowed = preferences[Keys.AUTHENTICATION_BEHAVIOR_ALLOWED] ?: false,
            remoteThemeEnabled = preferences[Keys.REMOTE_THEME_ENABLED] ?: false,
            themeColors = ThemeColors(
                primary = PredefinedColor.fromName(preferences[Keys.PRIMARY_COLOR] ?: "DEFAULT"),
                secondary = PredefinedColor.fromName(preferences[Keys.SECONDARY_COLOR] ?: "DEFAULT"),
                baseNormal = PredefinedColor.fromName(preferences[Keys.BASE_NORMAL_COLOR] ?: "DEFAULT"),
                baseLight = PredefinedColor.fromName(preferences[Keys.BASE_LIGHT_COLOR] ?: "DEFAULT"),
                baseDark = PredefinedColor.fromName(preferences[Keys.BASE_DARK_COLOR] ?: "DEFAULT"),
                baseShade = PredefinedColor.fromName(preferences[Keys.BASE_SHADE_COLOR] ?: "DEFAULT"),
                background = PredefinedColor.fromName(preferences[Keys.BACKGROUND_COLOR] ?: "DEFAULT"),
                systemNegative = PredefinedColor.fromName(preferences[Keys.SYSTEM_NEGATIVE_COLOR] ?: "DEFAULT")
            )
        )
    }

    suspend fun updateConfiguration(config: GliaConfiguration) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SITE_ID] = config.siteId
            preferences[Keys.API_KEY_ID] = config.apiKeyId
            preferences[Keys.API_KEY_SECRET] = config.apiKeySecret
            preferences[Keys.ENVIRONMENT] = config.environment.name.lowercase()
            preferences[Keys.CUSTOM_ENVIRONMENT_URL] = config.customEnvironmentUrl
            preferences[Keys.QUEUE_ID] = config.queueId
            preferences[Keys.VISITOR_CONTEXT_ASSET_ID] = config.visitorContextAssetId
            preferences[Keys.USE_DEFAULT_QUEUES] = config.useDefaultQueues
            preferences[Keys.SAVED_AUTH_TOKEN] = config.savedAuthToken
            preferences[Keys.COMPANY_NAME] = config.companyName
            preferences[Keys.MANUAL_LOCALE_OVERRIDE] = config.manualLocaleOverride
            preferences[Keys.ENABLE_BUBBLE_OUTSIDE_APP] = config.enableBubbleOutsideApp
            preferences[Keys.ENABLE_BUBBLE_INSIDE_APP] = config.enableBubbleInsideApp
            preferences[Keys.SUPPRESS_PUSH_NOTIFICATION_DIALOG] = config.suppressPushNotificationDialog
            preferences[Keys.AUTO_CONFIGURE_ENABLED] = config.autoConfigureEnabled
            preferences[Keys.AUTHENTICATION_BEHAVIOR_ALLOWED] = config.authenticationBehaviorAllowed
            preferences[Keys.REMOTE_THEME_ENABLED] = config.remoteThemeEnabled
            preferences[Keys.PRIMARY_COLOR] = config.themeColors.primary.name
            preferences[Keys.SECONDARY_COLOR] = config.themeColors.secondary.name
            preferences[Keys.BASE_NORMAL_COLOR] = config.themeColors.baseNormal.name
            preferences[Keys.BASE_LIGHT_COLOR] = config.themeColors.baseLight.name
            preferences[Keys.BASE_DARK_COLOR] = config.themeColors.baseDark.name
            preferences[Keys.BASE_SHADE_COLOR] = config.themeColors.baseShade.name
            preferences[Keys.BACKGROUND_COLOR] = config.themeColors.background.name
            preferences[Keys.SYSTEM_NEGATIVE_COLOR] = config.themeColors.systemNegative.name
        }
    }

    suspend fun updateSiteId(siteId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SITE_ID] = siteId
        }
    }

    suspend fun updateApiKeyId(apiKeyId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.API_KEY_ID] = apiKeyId
        }
    }

    suspend fun updateApiKeySecret(apiKeySecret: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.API_KEY_SECRET] = apiKeySecret
        }
    }

    suspend fun updateEnvironment(environment: EnvironmentSelection) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ENVIRONMENT] = environment.name.lowercase()
        }
    }

    suspend fun updateCustomEnvironmentUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CUSTOM_ENVIRONMENT_URL] = url
        }
    }

    suspend fun updateQueueId(queueId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.QUEUE_ID] = queueId
        }
    }

    suspend fun updateVisitorContextAssetId(assetId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.VISITOR_CONTEXT_ASSET_ID] = assetId
        }
    }

    suspend fun updateUseDefaultQueues(useDefaultQueues: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USE_DEFAULT_QUEUES] = useDefaultQueues
        }
    }

    suspend fun updateCompanyName(companyName: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.COMPANY_NAME] = companyName
        }
    }

    suspend fun updateManualLocaleOverride(locale: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MANUAL_LOCALE_OVERRIDE] = locale
        }
    }

    suspend fun updateBubbleOutsideApp(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ENABLE_BUBBLE_OUTSIDE_APP] = enabled
        }
    }

    suspend fun updateBubbleInsideApp(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ENABLE_BUBBLE_INSIDE_APP] = enabled
        }
    }

    suspend fun updateSuppressPushNotificationDialog(suppress: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SUPPRESS_PUSH_NOTIFICATION_DIALOG] = suppress
        }
    }

    suspend fun updateAutoConfigureEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTO_CONFIGURE_ENABLED] = enabled
        }
    }

    suspend fun updateAuthenticationBehaviorAllowed(allowed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTHENTICATION_BEHAVIOR_ALLOWED] = allowed
        }
    }

    suspend fun updateRemoteThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.REMOTE_THEME_ENABLED] = enabled
        }
    }

    suspend fun updateThemeColors(colors: ThemeColors) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PRIMARY_COLOR] = colors.primary.name
            preferences[Keys.SECONDARY_COLOR] = colors.secondary.name
            preferences[Keys.BASE_NORMAL_COLOR] = colors.baseNormal.name
            preferences[Keys.BASE_LIGHT_COLOR] = colors.baseLight.name
            preferences[Keys.BASE_DARK_COLOR] = colors.baseDark.name
            preferences[Keys.BASE_SHADE_COLOR] = colors.baseShade.name
            preferences[Keys.BACKGROUND_COLOR] = colors.background.name
            preferences[Keys.SYSTEM_NEGATIVE_COLOR] = colors.systemNegative.name
        }
    }

    suspend fun updateSavedAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SAVED_AUTH_TOKEN] = token
        }
    }

    /**
     * One-time migration from SharedPreferences to DataStore.
     * Called on app startup to migrate existing data.
     * After migration, SharedPreferences is never written to again.
     */
    suspend fun migrateFromSharedPreferencesIfNeeded() {
        val currentPrefs = context.dataStore.data.first()

        // Check if DataStore is empty (never been initialized)
        val isDataStoreEmpty = currentPrefs[Keys.SITE_ID] == null

        if (isDataStoreEmpty) {
            // Migrate from SharedPreferences
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            context.dataStore.edit { preferences ->
                // Migrate site configuration
                sharedPreferences.getString(context.getString(R.string.pref_site_id), null)?.let {
                    preferences[Keys.SITE_ID] = it
                }
                sharedPreferences.getString(context.getString(R.string.pref_api_key_id), null)?.let {
                    preferences[Keys.API_KEY_ID] = it
                }
                sharedPreferences.getString(context.getString(R.string.pref_api_key_secret), null)?.let {
                    preferences[Keys.API_KEY_SECRET] = it
                }
                sharedPreferences.getString(context.getString(R.string.pref_environment), null)?.let {
                    preferences[Keys.ENVIRONMENT] = it
                }

                // Migrate engagement settings
                sharedPreferences.getString(context.getString(R.string.pref_queue_id), null)?.let {
                    preferences[Keys.QUEUE_ID] = it
                }
                sharedPreferences.getString(context.getString(R.string.pref_context_asset_id), null)?.let {
                    preferences[Keys.VISITOR_CONTEXT_ASSET_ID] = it
                }

                // Migrate auth token
                sharedPreferences.getString(context.getString(R.string.pref_auth_token), null)?.let {
                    preferences[Keys.SAVED_AUTH_TOKEN] = it
                }

                // Migrate authentication settings
                if (sharedPreferences.contains(context.getString(R.string.pref_suppress_p_n_during_auth))) {
                    preferences[Keys.SUPPRESS_PUSH_NOTIFICATION_DIALOG] =
                        sharedPreferences.getBoolean(context.getString(R.string.pref_suppress_p_n_during_auth), false)
                }

                // Note: Theme colors will use defaults if not set in SharedPreferences
            }
        }
    }
}
