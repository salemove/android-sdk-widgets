package com.glia.exampleapp.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.glia.exampleapp.data.model.ConfigurationState
import com.glia.exampleapp.data.model.GliaConfiguration
import com.glia.exampleapp.data.repository.ConfigurationRepository
import com.glia.widgets.GliaWidgets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Represents the authentication state of the visitor
 */
sealed class AuthenticationState {
    data object Unauthenticated : AuthenticationState()
    data object Authenticated : AuthenticationState()
    data class Error(val message: String) : AuthenticationState()
}

/**
 * Represents the push notification permission state
 */
enum class PushPermissionState {
    GRANTED,
    NOT_GRANTED,
    NOT_REQUIRED // Android < 13
}

/**
 * SDK version information
 */
data class SdkVersionInfo(
    val widgetsSdkVersion: String = "",
    val coreSdkVersion: String = ""
)

/**
 * Centralized application state management, similar to iOS AppState pattern.
 * Manages SDK configuration, authentication, and other app-wide state.
 */
class AppState(context: Context) {
    private val applicationContext = context.applicationContext

    val configurationRepository = ConfigurationRepository(applicationContext)

    // Configuration
    val configuration: Flow<GliaConfiguration> = configurationRepository.configuration

    // SDK Configuration State
    private val _configurationState = MutableStateFlow<ConfigurationState>(ConfigurationState.Idle)
    val configurationState: StateFlow<ConfigurationState> = _configurationState.asStateFlow()

    // Authentication State
    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.Unauthenticated)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState.asStateFlow()

    // Restart Required State - tracks if settings requiring restart have been changed
    private val _restartRequired = MutableStateFlow(false)
    val restartRequired: StateFlow<Boolean> = _restartRequired.asStateFlow()

    // Original configuration values that require restart
    private var originalSiteId: String? = null
    private var originalBubbleOutsideApp: Boolean? = null
    private var originalBubbleInsideApp: Boolean? = null

    // SDK Version Information
    private val _sdkVersionInfo = MutableStateFlow(SdkVersionInfo())
    val sdkVersionInfo: StateFlow<SdkVersionInfo> = _sdkVersionInfo.asStateFlow()

    // Live Observation State
    private val _liveObservationPaused = MutableStateFlow(false)
    val liveObservationPaused: StateFlow<Boolean> = _liveObservationPaused.asStateFlow()

    init {
        fetchSdkVersionInfo()
    }

    fun setConfigurationState(state: ConfigurationState) {
        _configurationState.value = state
    }

    fun setAuthenticationState(state: AuthenticationState) {
        _authenticationState.value = state
    }

    fun setLiveObservationPaused(paused: Boolean) {
        _liveObservationPaused.value = paused
    }

    /**
     * Initialize tracking of settings that require restart.
     * Should be called after loading configuration.
     */
    suspend fun initializeRestartTracking() {
        val config = configuration.first()
        originalSiteId = config.siteId
        originalBubbleOutsideApp = config.enableBubbleOutsideApp
        originalBubbleInsideApp = config.enableBubbleInsideApp
        _restartRequired.value = false
    }

    /**
     * Check if restart is required based on changed settings.
     */
    suspend fun checkRestartRequired() {
        val config = configuration.first()
        _restartRequired.value = originalSiteId != null && (
            config.siteId != originalSiteId ||
                config.enableBubbleOutsideApp != originalBubbleOutsideApp ||
                config.enableBubbleInsideApp != originalBubbleInsideApp
        )
    }

    /**
     * Get the current push notification permission state.
     */
    fun getPushPermissionState(): PushPermissionState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) PushPermissionState.GRANTED else PushPermissionState.NOT_GRANTED
        } else {
            PushPermissionState.NOT_REQUIRED
        }
    }

    private fun fetchSdkVersionInfo() {
        try {
            _sdkVersionInfo.value = SdkVersionInfo(
                widgetsSdkVersion = GliaWidgets.widgetsSdkVersion,
                coreSdkVersion = GliaWidgets.widgetsCoreSdkVersion
            )
        } catch (e: Exception) {
            // SDK not initialized yet, versions will be fetched later
        }
    }

    /**
     * Refresh SDK version information after SDK is initialized.
     */
    fun refreshSdkVersionInfo() {
        fetchSdkVersionInfo()
    }

    companion object {
        @Volatile
        private var instance: AppState? = null

        fun getInstance(context: Context): AppState {
            return instance ?: synchronized(this) {
                instance ?: AppState(context).also { instance = it }
            }
        }
    }
}
