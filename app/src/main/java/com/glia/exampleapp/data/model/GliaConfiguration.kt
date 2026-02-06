package com.glia.exampleapp.data.model

/**
 * Complete configuration for the Glia SDK testing app.
 * All fields are stored in DataStore for persistence.
 */
data class GliaConfiguration(
    // Site Configuration
    val siteId: String = "",
    val apiKeyId: String = "",
    val apiKeySecret: String = "",
    val environment: EnvironmentSelection = EnvironmentSelection.BETA,
    val customEnvironmentUrl: String = "",

    // Engagement Settings
    val queueId: String = "",
    val visitorContextAssetId: String = "",
    val useDefaultQueues: Boolean = false,

    // Authentication
    val savedAuthToken: String = "",

    // Company Settings
    val companyName: String = "Glia",
    val manualLocaleOverride: String = "",

    // Bubble Settings (require restart)
    val enableBubbleOutsideApp: Boolean = true,
    val enableBubbleInsideApp: Boolean = true,

    // Authentication Settings
    val suppressPushNotificationDialog: Boolean = false,
    val autoConfigureEnabled: Boolean = true,
    val authenticationBehaviorAllowed: Boolean = false,

    // Theme Settings
    val remoteThemeEnabled: Boolean = false,
    val themeColors: ThemeColors = ThemeColors()
)
