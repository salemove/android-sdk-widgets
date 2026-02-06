package com.glia.exampleapp.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.glia.exampleapp.data.AppState
import com.glia.exampleapp.data.PushPermissionState
import com.glia.exampleapp.data.SdkVersionInfo
import com.glia.exampleapp.data.model.EnvironmentSelection
import com.glia.exampleapp.data.model.GliaConfiguration
import com.glia.exampleapp.data.model.PredefinedColor
import com.glia.exampleapp.data.model.ThemeColors
import com.glia.widgets.GliaWidgets
import com.glia.widgets.queue.Queue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * State for queue picker loading
 */
sealed class SettingsQueuesState {
    data object Idle : SettingsQueuesState()
    data object Loading : SettingsQueuesState()
    data class Loaded(val queues: List<Queue>) : SettingsQueuesState()
    data object Empty : SettingsQueuesState()
    data class Error(val message: String) : SettingsQueuesState()
}

/**
 * UI state for the settings screen
 */
data class SettingsUiState(
    // Site Configuration
    val siteId: String = "",
    val apiKeyId: String = "",
    val apiKeySecret: String = "",
    val environment: EnvironmentSelection = EnvironmentSelection.BETA,
    val customEnvironmentUrl: String = "",

    // Engagement Settings
    val queueId: String = "",
    val visitorContextAssetId: String = "",

    // Company Settings
    val companyName: String = "Glia",
    val manualLocaleOverride: String = "",

    // Bubble Settings
    val enableBubbleInsideApp: Boolean = true,

    // Authentication Settings
    val suppressPushNotificationDialog: Boolean = false,
    val autoConfigureEnabled: Boolean = true,
    val authenticationBehaviorAllowed: Boolean = false,

    // Theme Settings
    val themeColors: ThemeColors = ThemeColors(),

    // SDK Version Info
    val sdkVersionInfo: SdkVersionInfo = SdkVersionInfo(),

    // Push Notification State
    val pushPermissionState: PushPermissionState = PushPermissionState.NOT_REQUIRED,

    // Queue Picker State
    val queuesState: SettingsQueuesState = SettingsQueuesState.Idle,

    // Restart Required
    val restartRequired: Boolean = false,

    // Tracks if any changes were made
    val hasChanges: Boolean = false,

    // Loading state
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val applicationContext: Context,
    private val appState: AppState
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Store original values to detect changes requiring restart
    private var originalSiteId: String = ""
    private var originalBubbleInsideApp: Boolean = true

    init {
        loadConfiguration()
        observeAppState()
    }

    private fun loadConfiguration() {
        viewModelScope.launch {
            val config = appState.configuration.first()
            android.util.Log.d("SettingsViewModel", "Loading configuration: siteId=${config.siteId}, apiKeyId=${config.apiKeyId}, environment=${config.environment}")
            originalSiteId = config.siteId
            originalBubbleInsideApp = config.enableBubbleInsideApp

            _uiState.value = _uiState.value.copy(
                siteId = config.siteId,
                apiKeyId = config.apiKeyId,
                apiKeySecret = config.apiKeySecret,
                environment = config.environment,
                customEnvironmentUrl = config.customEnvironmentUrl,
                queueId = config.queueId,
                visitorContextAssetId = config.visitorContextAssetId,
                companyName = config.companyName,
                manualLocaleOverride = config.manualLocaleOverride,
                enableBubbleInsideApp = config.enableBubbleInsideApp,
                suppressPushNotificationDialog = config.suppressPushNotificationDialog,
                autoConfigureEnabled = config.autoConfigureEnabled,
                authenticationBehaviorAllowed = config.authenticationBehaviorAllowed,
                themeColors = config.themeColors,
                sdkVersionInfo = appState.sdkVersionInfo.value,
                pushPermissionState = appState.getPushPermissionState(),
                isLoading = false
            )
        }
    }

    private fun observeAppState() {
        viewModelScope.launch {
            appState.sdkVersionInfo.collect { info ->
                _uiState.value = _uiState.value.copy(sdkVersionInfo = info)
            }
        }
    }

    // Environment
    fun updateEnvironment(environment: EnvironmentSelection) {
        _uiState.value = _uiState.value.copy(
            environment = environment,
            hasChanges = true
        )
    }

    fun updateCustomEnvironmentUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            customEnvironmentUrl = url,
            hasChanges = true
        )
    }

    // Site Configuration
    fun updateSiteId(siteId: String) {
        _uiState.value = _uiState.value.copy(
            siteId = siteId,
            hasChanges = true,
            restartRequired = siteId != originalSiteId || _uiState.value.enableBubbleInsideApp != originalBubbleInsideApp
        )
    }

    fun updateApiKeyId(apiKeyId: String) {
        _uiState.value = _uiState.value.copy(
            apiKeyId = apiKeyId,
            hasChanges = true
        )
    }

    fun updateApiKeySecret(apiKeySecret: String) {
        _uiState.value = _uiState.value.copy(
            apiKeySecret = apiKeySecret,
            hasChanges = true
        )
    }

    fun updateQueueId(queueId: String) {
        _uiState.value = _uiState.value.copy(
            queueId = queueId,
            hasChanges = true
        )
    }

    fun updateVisitorContextAssetId(assetId: String) {
        _uiState.value = _uiState.value.copy(
            visitorContextAssetId = assetId,
            hasChanges = true
        )
    }

    fun updateCompanyName(name: String) {
        _uiState.value = _uiState.value.copy(
            companyName = name,
            hasChanges = true
        )
    }

    fun updateManualLocaleOverride(locale: String) {
        _uiState.value = _uiState.value.copy(
            manualLocaleOverride = locale,
            hasChanges = true
        )
    }

    // Authentication Settings
    fun updateAutoConfigureEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            autoConfigureEnabled = enabled,
            hasChanges = true
        )
    }

    fun updateAuthenticationBehaviorAllowed(allowed: Boolean) {
        _uiState.value = _uiState.value.copy(
            authenticationBehaviorAllowed = allowed,
            hasChanges = true
        )
    }

    fun updateSuppressPushPermission(suppress: Boolean) {
        _uiState.value = _uiState.value.copy(
            suppressPushNotificationDialog = suppress,
            hasChanges = true
        )
    }

    // Features
    fun updateBubbleInsideApp(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            enableBubbleInsideApp = enabled,
            hasChanges = true,
            restartRequired = _uiState.value.siteId != originalSiteId || enabled != originalBubbleInsideApp
        )
    }

    // Theme Colors
    fun updatePrimaryColor(color: PredefinedColor) {
        _uiState.value = _uiState.value.copy(
            themeColors = _uiState.value.themeColors.copy(primary = color),
            hasChanges = true
        )
    }

    fun updateSecondaryColor(color: PredefinedColor) {
        _uiState.value = _uiState.value.copy(
            themeColors = _uiState.value.themeColors.copy(secondary = color),
            hasChanges = true
        )
    }

    fun updateBaseNormalColor(color: PredefinedColor) {
        _uiState.value = _uiState.value.copy(
            themeColors = _uiState.value.themeColors.copy(baseNormal = color),
            hasChanges = true
        )
    }

    fun updateBaseLightColor(color: PredefinedColor) {
        _uiState.value = _uiState.value.copy(
            themeColors = _uiState.value.themeColors.copy(baseLight = color),
            hasChanges = true
        )
    }

    fun updateBaseDarkColor(color: PredefinedColor) {
        _uiState.value = _uiState.value.copy(
            themeColors = _uiState.value.themeColors.copy(baseDark = color),
            hasChanges = true
        )
    }

    fun updateBaseShadeColor(color: PredefinedColor) {
        _uiState.value = _uiState.value.copy(
            themeColors = _uiState.value.themeColors.copy(baseShade = color),
            hasChanges = true
        )
    }

    fun updateSystemNegativeColor(color: PredefinedColor) {
        _uiState.value = _uiState.value.copy(
            themeColors = _uiState.value.themeColors.copy(systemNegative = color),
            hasChanges = true
        )
    }

    // Queue Picker
    fun loadQueues() {
        _uiState.value = _uiState.value.copy(queuesState = SettingsQueuesState.Loading)

        try {
            GliaWidgets.getQueues(
                { queues ->
                    _uiState.value = if (queues.isEmpty()) {
                        _uiState.value.copy(queuesState = SettingsQueuesState.Empty)
                    } else {
                        _uiState.value.copy(queuesState = SettingsQueuesState.Loaded(queues.toList()))
                    }
                },
                { error ->
                    _uiState.value = _uiState.value.copy(
                        queuesState = SettingsQueuesState.Error(error?.message ?: "Failed to load queues")
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                queuesState = SettingsQueuesState.Error("SDK not initialized")
            )
        }
    }

    fun selectQueue(queue: Queue) {
        _uiState.value = _uiState.value.copy(
            queueId = queue.id,
            queuesState = SettingsQueuesState.Idle,
            hasChanges = true
        )
    }

    fun dismissQueuePicker() {
        _uiState.value = _uiState.value.copy(queuesState = SettingsQueuesState.Idle)
    }

    // Push Permission
    fun refreshPushPermissionState() {
        _uiState.value = _uiState.value.copy(
            pushPermissionState = appState.getPushPermissionState()
        )
    }

    // Save Configuration
    fun saveConfiguration() {
        viewModelScope.launch {
            val state = _uiState.value
            val config = GliaConfiguration(
                siteId = state.siteId,
                apiKeyId = state.apiKeyId,
                apiKeySecret = state.apiKeySecret,
                environment = state.environment,
                customEnvironmentUrl = state.customEnvironmentUrl,
                queueId = state.queueId,
                visitorContextAssetId = state.visitorContextAssetId,
                companyName = state.companyName,
                manualLocaleOverride = state.manualLocaleOverride,
                enableBubbleOutsideApp = true, // Always true on Android
                enableBubbleInsideApp = state.enableBubbleInsideApp,
                suppressPushNotificationDialog = state.suppressPushNotificationDialog,
                autoConfigureEnabled = state.autoConfigureEnabled,
                authenticationBehaviorAllowed = state.authenticationBehaviorAllowed,
                themeColors = state.themeColors
            )
            appState.configurationRepository.updateConfiguration(config)
            _uiState.value = _uiState.value.copy(hasChanges = false)
        }
    }

    class Factory(
        private val applicationContext: Context,
        private val appState: AppState
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(applicationContext, appState) as T
        }
    }
}
