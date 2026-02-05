package com.glia.exampleapp.ui.screens.main

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.exampleapp.ExampleAppConfigManager
import com.glia.exampleapp.R
import com.glia.exampleapp.data.AppState
import com.glia.exampleapp.data.AuthenticationState
import com.glia.exampleapp.data.model.ConfigurationState
import com.glia.widgets.GliaWidgets
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.authentication.Authentication
import com.glia.widgets.entrywidget.EntryWidget
import com.glia.widgets.launcher.EngagementLauncher
import com.glia.widgets.queue.Queue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State for queues loading
 */
sealed class QueuesState {
    data object Idle : QueuesState()
    data object Loading : QueuesState()
    data class Loaded(val queues: List<Queue>) : QueuesState()
    data object Empty : QueuesState()
    data class Error(val message: String) : QueuesState()
}

/**
 * UI state for the main screen
 */
data class MainUiState(
    val configurationState: ConfigurationState = ConfigurationState.Idle,
    val authenticationState: AuthenticationState = AuthenticationState.Unauthenticated,
    val isLiveObservationPaused: Boolean = false,
    val useDefaultQueues: Boolean = false,
    val showEntryWidgetEmbedded: Boolean = false,
    val showVisitorCodeEmbedded: Boolean = false,
    val embeddedViewExpanded: Boolean = true,
    val queuesState: QueuesState = QueuesState.Idle,
    val errorMessage: String? = null
)

class MainViewModel(
    private val applicationContext: Context,
    private val appState: AppState
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var authentication: Authentication? = null
    private var engagementLauncher: EngagementLauncher? = null
    private var entryWidget: EntryWidget? = null

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    init {
        observeAppState()
        loadDefaultQueuesState()
    }

    private fun loadDefaultQueuesState() {
        val useDefaultQueues = sharedPreferences.getBoolean(
            applicationContext.getString(R.string.pref_default_queues),
            false
        )
        _uiState.value = _uiState.value.copy(useDefaultQueues = useDefaultQueues)
    }

    private fun observeAppState() {
        viewModelScope.launch {
            appState.configurationState.collect { state ->
                _uiState.value = _uiState.value.copy(configurationState = state)
            }
        }
        viewModelScope.launch {
            appState.authenticationState.collect { state ->
                _uiState.value = _uiState.value.copy(authenticationState = state)
            }
        }
        viewModelScope.launch {
            appState.liveObservationPaused.collect { paused ->
                _uiState.value = _uiState.value.copy(isLiveObservationPaused = paused)
            }
        }
    }

    fun initializeSdk() {
        if (GliaWidgets.isInitialized()) {
            appState.setConfigurationState(ConfigurationState.Configured)
            prepareAuthentication()
            listenForCallVisualizerEngagements()
            return
        }

        appState.setConfigurationState(ConfigurationState.Loading)

        GliaWidgets.init(
            ExampleAppConfigManager.createDefaultConfig(applicationContext),
            onComplete = {
                appState.setConfigurationState(ConfigurationState.Configured)
                prepareAuthentication()
                listenForCallVisualizerEngagements()
                setupEngagementListeners()
                appState.refreshSdkVersionInfo()
            },
            onError = { error ->
                appState.setConfigurationState(ConfigurationState.Error(error.message ?: "Unknown error"))
                showError(error.message ?: "SDK initialization failed")
            }
        )
    }

    private fun prepareAuthentication() {
        val behavior = getAuthenticationBehaviorFromPrefs()
        authentication = GliaWidgets.getAuthentication(behavior)
        updateAuthenticationState()
    }

    private fun getAuthenticationBehaviorFromPrefs(): Authentication.Behavior {
        val allowedValue = applicationContext.getString(R.string.authentication_behavior_allowed_during_engagement)
        val valueFromPrefs = sharedPreferences.getString(
            applicationContext.getString(R.string.pref_authentication_behavior),
            allowedValue
        )
        return if (valueFromPrefs == allowedValue) {
            Authentication.Behavior.ALLOWED_DURING_ENGAGEMENT
        } else {
            Authentication.Behavior.FORBIDDEN_DURING_ENGAGEMENT
        }
    }

    private fun updateAuthenticationState() {
        val isAuthenticated = authentication?.isAuthenticated == true
        appState.setAuthenticationState(
            if (isAuthenticated) AuthenticationState.Authenticated
            else AuthenticationState.Unauthenticated
        )
    }

    private fun listenForCallVisualizerEngagements() {
        GliaWidgets.getCallVisualizer().onEngagementStart {
            // Hide embedded visitor code when engagement starts
            _uiState.value = _uiState.value.copy(showVisitorCodeEmbedded = false)
        }
    }

    private fun setupEngagementListeners() {
        Glia.on(Glia.Events.ENGAGEMENT) { engagement ->
            engagement.on(Engagement.Events.END) {
                // Engagement ended
            }
        }

        Glia.omnibrowse.on(Omnibrowse.Events.ENGAGEMENT) { engagement ->
            engagement.on(Engagement.Events.END) {
                // Omnibrowse engagement ended
            }
        }
    }

    private fun getEngagementLauncher(): EngagementLauncher {
        if (engagementLauncher == null) {
            engagementLauncher = GliaWidgets.getEngagementLauncher(getQueueIds())
        }
        return engagementLauncher!!
    }

    fun getEntryWidget(): EntryWidget {
        if (entryWidget == null) {
            entryWidget = GliaWidgets.getEntryWidget(getQueueIds())
        }
        return entryWidget!!
    }

    private fun getQueueIds(): List<String> {
        val useDefaultQueues = sharedPreferences.getBoolean(
            applicationContext.getString(R.string.pref_default_queues),
            false
        )
        if (useDefaultQueues) {
            return emptyList()
        }
        return listOf(getQueueId())
    }

    private fun getQueueId(): String {
        return sharedPreferences.getString(
            applicationContext.getString(R.string.pref_queue_id),
            applicationContext.getString(R.string.glia_queue_id)
        ) ?: applicationContext.getString(R.string.glia_queue_id)
    }

    private fun getVisitorContextAssetId(): String? {
        return sharedPreferences.getString(
            applicationContext.getString(R.string.pref_context_asset_id),
            null
        )
    }

    // Engagement methods
    fun startChat(activity: Activity) {
        runCatching {
            val assetId = getVisitorContextAssetId()
            if (assetId != null) {
                getEngagementLauncher().startChat(activity, assetId)
            } else {
                getEngagementLauncher().startChat(activity)
            }
        }.onFailure { showError(it.message ?: "Failed to start chat") }
    }

    fun startAudioCall(activity: Activity) {
        runCatching {
            val assetId = getVisitorContextAssetId()
            if (assetId != null) {
                getEngagementLauncher().startAudioCall(activity, assetId)
            } else {
                getEngagementLauncher().startAudioCall(activity)
            }
        }.onFailure { showError(it.message ?: "Failed to start audio call") }
    }

    fun startVideoCall(activity: Activity) {
        runCatching {
            val assetId = getVisitorContextAssetId()
            if (assetId != null) {
                getEngagementLauncher().startVideoCall(activity, assetId)
            } else {
                getEngagementLauncher().startVideoCall(activity)
            }
        }.onFailure { showError(it.message ?: "Failed to start video call") }
    }

    fun startSecureMessaging(activity: Activity) {
        runCatching {
            val assetId = getVisitorContextAssetId()
            if (assetId != null) {
                getEngagementLauncher().startSecureMessaging(activity, assetId)
            } else {
                getEngagementLauncher().startSecureMessaging(activity)
            }
        }.onFailure { showError(it.message ?: "Failed to start secure messaging") }
    }

    fun endEngagement() {
        GliaWidgets.endEngagement()
    }

    // Entry Widget methods
    fun showEntryWidgetSheet(activity: Activity) {
        getEntryWidget().show(activity)
    }

    fun toggleEntryWidgetEmbedded(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(showEntryWidgetEmbedded = enabled)
    }

    // Visitor Code methods
    fun showVisitorCodeDialog() {
        val assetId = getVisitorContextAssetId()
        val callVisualizer = GliaWidgets.getCallVisualizer()
        if (!assetId.isNullOrBlank()) {
            callVisualizer.addVisitorContext(assetId)
        }
        callVisualizer.showVisitorCodeDialog()
    }

    fun toggleVisitorCodeEmbedded(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(showVisitorCodeEmbedded = enabled)
    }

    fun toggleEmbeddedViewExpanded() {
        _uiState.value = _uiState.value.copy(
            embeddedViewExpanded = !_uiState.value.embeddedViewExpanded
        )
    }

    // Authentication methods
    fun authenticate(jwtToken: String, externalAccessToken: String?, onSuccess: () -> Unit) {
        prepareAuthentication()
        authentication?.authenticate(
            jwtToken,
            externalAccessToken,
            {
                updateAuthenticationState()
                saveAuthToken(jwtToken)
                onSuccess()
            },
            { exception ->
                if (exception.gliaCause == GliaWidgetsException.Cause.AUTHENTICATION_ERROR) {
                    showError(exception.message ?: "Authentication error")
                } else {
                    showError("Error: $exception")
                }
                appState.setAuthenticationState(
                    AuthenticationState.Error(exception.message ?: "Authentication failed")
                )
            }
        )
    }

    fun deauthenticate(stopPushNotifications: Boolean, onSuccess: () -> Unit) {
        authentication?.deauthenticate(
            stopPushNotifications,
            {
                updateAuthenticationState()
                onSuccess()
            },
            { exception ->
                showError("Error: $exception")
            }
        )
    }

    fun refreshAuthentication(jwtToken: String, externalAccessToken: String?) {
        authentication?.refresh(
            jwtToken,
            externalAccessToken,
            {
                updateAuthenticationState()
                saveAuthToken(jwtToken)
                showToast("Token refreshed successfully")
            },
            { exception ->
                showError("Error: $exception")
            }
        )
    }

    private fun saveAuthToken(jwt: String) {
        val defaultToken = applicationContext.getString(R.string.glia_jwt)
        if (jwt != defaultToken) {
            sharedPreferences.edit()
                .putString(applicationContext.getString(R.string.pref_auth_token), jwt)
                .apply()
        }
    }

    fun getSavedAuthToken(): String {
        val savedToken = sharedPreferences.getString(
            applicationContext.getString(R.string.pref_auth_token),
            ""
        ) ?: ""
        return savedToken.ifEmpty { applicationContext.getString(R.string.glia_jwt) }
    }

    fun clearAuthToken() {
        sharedPreferences.edit()
            .putString(applicationContext.getString(R.string.pref_auth_token), null)
            .apply()
    }

    // Live Observation methods
    fun toggleLiveObservation() {
        val currentlyPaused = _uiState.value.isLiveObservationPaused
        if (currentlyPaused) {
            Glia.getLiveObservation().resume()
        } else {
            Glia.getLiveObservation().pause()
        }
        appState.setLiveObservationPaused(!currentlyPaused)
    }

    // Default Queues toggle
    fun toggleDefaultQueues() {
        val newValue = !_uiState.value.useDefaultQueues
        sharedPreferences.edit()
            .putBoolean(applicationContext.getString(R.string.pref_default_queues), newValue)
            .apply()
        _uiState.value = _uiState.value.copy(useDefaultQueues = newValue)
        // Reset engagement launcher and entry widget to pick up new queue settings
        engagementLauncher = null
        entryWidget = null
    }

    // Queue picker methods
    fun loadQueues() {
        _uiState.value = _uiState.value.copy(queuesState = QueuesState.Loading)

        GliaWidgets.getQueues(
            { queues ->
                _uiState.value = if (queues.isEmpty()) {
                    _uiState.value.copy(queuesState = QueuesState.Empty)
                } else {
                    _uiState.value.copy(queuesState = QueuesState.Loaded(queues.toList()))
                }
            },
            { error ->
                _uiState.value = _uiState.value.copy(
                    queuesState = QueuesState.Error(error?.message ?: "Failed to load queues")
                )
            }
        )
    }

    fun selectQueue(queue: Queue) {
        sharedPreferences.edit()
            .putString(applicationContext.getString(R.string.pref_queue_id), queue.id)
            .apply()
        // Reset engagement launcher to pick up new queue
        engagementLauncher = null
        entryWidget = null
        _uiState.value = _uiState.value.copy(queuesState = QueuesState.Idle)
    }

    fun dismissQueuePicker() {
        _uiState.value = _uiState.value.copy(queuesState = QueuesState.Idle)
    }

    // Clear session
    fun clearSession() {
        GliaWidgets.clearVisitorSession()
        updateAuthenticationState()
    }

    // Error handling
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val applicationContext: Context,
        private val appState: AppState
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(applicationContext, appState) as T
        }
    }
}
