package com.glia.exampleapp.ui.screens.main

import android.app.Activity
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.glia.exampleapp.data.AuthenticationState
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme
import com.glia.exampleapp.data.model.ConfigurationState
import com.glia.exampleapp.ui.components.ActionButton
import com.glia.exampleapp.ui.components.CollapsibleEmbeddedContainer
import com.glia.exampleapp.ui.components.EngagementButton
import com.glia.exampleapp.ui.components.GliaLogo
import com.glia.exampleapp.ui.components.SectionHeader
import com.glia.widgets.GliaWidgets
import com.glia.widgets.queue.Queue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToVisitorInfo: () -> Unit,
    onNavigateToSensitiveData: () -> Unit,
    onNavigateToLegacyActivity: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity
    val snackbarHostState = remember { SnackbarHostState() }

    var showActionsMenu by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showDeauthDialog by remember { mutableStateOf(false) }
    var showRefreshAuthDialog by remember { mutableStateOf(false) }
    var showQueuePicker by remember { mutableStateOf(false) }

    // Show error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("main_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        GliaLogo(size = 60.dp)
                    }
                },
                actions = {
                    // Configure SDK button
                    ConfigureSdkButton(
                        configurationState = uiState.configurationState,
                        onClick = { viewModel.initializeSdk() },
                        modifier = Modifier.testTag("main_configure_sdk_button")
                    )

                    // Actions menu
                    Box {
                        IconButton(
                            onClick = { showActionsMenu = true },
                            modifier = Modifier.testTag("main_actions_menu")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                        ActionsDropdownMenu(
                            expanded = showActionsMenu,
                            onDismiss = { showActionsMenu = false },
                            onVisitorInfo = {
                                showActionsMenu = false
                                onNavigateToVisitorInfo()
                            },
                            onSensitiveData = {
                                showActionsMenu = false
                                onNavigateToSensitiveData()
                            },
                            onLegacyActivity = {
                                showActionsMenu = false
                                onNavigateToLegacyActivity()
                            },
                            onEndEngagement = {
                                showActionsMenu = false
                                viewModel.endEngagement()
                            },
                            onClearSession = {
                                showActionsMenu = false
                                viewModel.clearSession()
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Default Queues Toggle
            DefaultQueuesToggle(
                useDefaultQueues = uiState.useDefaultQueues,
                onToggle = { viewModel.toggleDefaultQueues() },
                modifier = Modifier.testTag("settings_default_queues_switch")
            )

            Spacer(Modifier.height(16.dp))

            // Engagement Buttons
            SectionHeader("Engagements")
            EngagementButtonsRow(
                enabled = uiState.configurationState is ConfigurationState.Configured,
                onChatClick = { viewModel.startChat(activity) },
                onAudioClick = { viewModel.startAudioCall(activity) },
                onVideoClick = { viewModel.startVideoCall(activity) },
                onSecureMessagingClick = { viewModel.startSecureMessaging(activity) }
            )

            Spacer(Modifier.height(16.dp))

            // Authentication Section
            SectionHeader("Authentication")
            AuthenticationSection(
                authState = uiState.authenticationState,
                configState = uiState.configurationState,
                onAuthenticateClick = { showAuthDialog = true },
                onDeauthenticateClick = { showDeauthDialog = true },
                onRefreshClick = { showRefreshAuthDialog = true }
            )

            Spacer(Modifier.height(16.dp))

            // Entry Widget Section
            SectionHeader("Entry Widget")
            EntryWidgetSection(
                enabled = uiState.configurationState is ConfigurationState.Configured,
                expanded = uiState.showEntryWidgetEmbedded,
                onShowSheetClick = { viewModel.showEntryWidgetSheet(activity) },
                onToggleEmbedded = { viewModel.toggleEntryWidgetEmbedded(!uiState.showEntryWidgetEmbedded) },
                viewModel = viewModel
            )

            Spacer(Modifier.height(16.dp))

            // Call Visualizer / Visitor Code Section
            SectionHeader("Call Visualizer")
            VisitorCodeSection(
                enabled = uiState.configurationState is ConfigurationState.Configured,
                expanded = uiState.showVisitorCodeEmbedded,
                onShowDialogClick = { viewModel.showVisitorCodeDialog() },
                onToggleEmbedded = { viewModel.toggleVisitorCodeEmbedded(!uiState.showVisitorCodeEmbedded) }
            )

            Spacer(Modifier.height(16.dp))

            // Queue Picker
            ActionButton(
                text = "Select Queue",
                onClick = {
                    viewModel.loadQueues()
                    showQueuePicker = true
                },
                enabled = uiState.configurationState is ConfigurationState.Configured,
                testTagId = "main_queue_picker_button"
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    // Dialogs
    if (showAuthDialog) {
        AuthenticationDialog(
            initialToken = viewModel.getSavedAuthToken(),
            onDismiss = { showAuthDialog = false },
            onAuthenticate = { jwt, accessToken ->
                viewModel.authenticate(jwt, accessToken) {
                    showAuthDialog = false
                }
            },
            onClearToken = { viewModel.clearAuthToken() }
        )
    }

    if (showDeauthDialog) {
        DeauthenticationDialog(
            onDismiss = { showDeauthDialog = false },
            onDeauthenticate = { stopPush ->
                viewModel.deauthenticate(stopPush) {
                    showDeauthDialog = false
                }
            }
        )
    }

    if (showRefreshAuthDialog) {
        RefreshAuthDialog(
            initialToken = viewModel.getSavedAuthToken(),
            onDismiss = { showRefreshAuthDialog = false },
            onRefresh = { jwt, accessToken ->
                viewModel.refreshAuthentication(jwt, accessToken)
                showRefreshAuthDialog = false
            },
            onClearToken = { viewModel.clearAuthToken() }
        )
    }

    if (showQueuePicker) {
        QueuePickerDialog(
            queuesState = uiState.queuesState,
            onDismiss = {
                showQueuePicker = false
                viewModel.dismissQueuePicker()
            },
            onQueueSelected = { queue ->
                viewModel.selectQueue(queue)
                showQueuePicker = false
            },
            onRetry = { viewModel.loadQueues() }
        )
    }
}

@Composable
private fun ConfigureSdkButton(
    configurationState: ConfigurationState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        when (configurationState) {
            is ConfigurationState.Idle -> Icon(Icons.Default.PlayArrow, "Configure SDK")
            is ConfigurationState.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            is ConfigurationState.Configured -> Icon(
                Icons.Default.Check,
                "SDK Configured",
                tint = MaterialTheme.colorScheme.primary
            )
            is ConfigurationState.Error -> Icon(
                Icons.Default.Warning,
                "Configuration Error",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ActionsDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onVisitorInfo: () -> Unit,
    onSensitiveData: () -> Unit,
    onLegacyActivity: () -> Unit,
    onEndEngagement: () -> Unit,
    onClearSession: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Visitor Info") },
            onClick = onVisitorInfo
        )
        DropdownMenuItem(
            text = { Text("Sensitive Data (LO Test)") },
            onClick = onSensitiveData
        )
        DropdownMenuItem(
            text = { Text("Legacy Activity") },
            onClick = onLegacyActivity
        )
        DropdownMenuItem(
            text = { Text("End Engagement") },
            onClick = onEndEngagement
        )
        DropdownMenuItem(
            text = { Text("Clear Session") },
            onClick = onClearSession
        )
    }
}

@Composable
private fun DefaultQueuesToggle(
    useDefaultQueues: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Use Default Queues",
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = useDefaultQueues,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun EngagementButtonsRow(
    enabled: Boolean,
    onChatClick: () -> Unit,
    onAudioClick: () -> Unit,
    onVideoClick: () -> Unit,
    onSecureMessagingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        EngagementButton(
            text = "Chat",
            icon = Icons.Default.Email,
            onClick = onChatClick,
            enabled = enabled,
            testTagId = "main_chat_button",
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        EngagementButton(
            text = "Audio",
            icon = Icons.Default.Call,
            onClick = onAudioClick,
            enabled = enabled,
            testTagId = "main_audio_button",
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        EngagementButton(
            text = "Video",
            icon = Icons.Default.Face,
            onClick = onVideoClick,
            enabled = enabled,
            testTagId = "main_video_button",
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        EngagementButton(
            text = "Secure",
            icon = Icons.AutoMirrored.Filled.Send,
            onClick = onSecureMessagingClick,
            enabled = enabled,
            testTagId = "main_secure_messaging_button",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AuthenticationSection(
    authState: AuthenticationState,
    configState: ConfigurationState,
    onAuthenticateClick: () -> Unit,
    onDeauthenticateClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val isConfigured = configState is ConfigurationState.Configured
    val isAuthenticated = authState is AuthenticationState.Authenticated

    if (!isConfigured) {
        ActionButton(
            text = "Configure SDK First",
            onClick = {},
            enabled = false
        )
    } else if (isAuthenticated) {
        ActionButton(
            text = "Deauthenticate",
            onClick = onDeauthenticateClick,
            testTagId = "main_toggle_authenticate_button"
        )
        ActionButton(
            text = "Refresh Access Token",
            onClick = onRefreshClick,
            testTagId = "main_refresh_access_token_button"
        )
    } else {
        ActionButton(
            text = "Authenticate",
            onClick = onAuthenticateClick,
            testTagId = "main_toggle_authenticate_button"
        )
    }
}

@Composable
private fun EntryWidgetSection(
    enabled: Boolean,
    expanded: Boolean,
    onShowSheetClick: () -> Unit,
    onToggleEmbedded: () -> Unit,
    viewModel: MainViewModel
) {
    ActionButton(
        text = "Show Sheet",
        onClick = onShowSheetClick,
        enabled = enabled,
        testTagId = "main_entry_widget_sheet_button"
    )

    CollapsibleEmbeddedContainer(
        title = "Embedded View",
        expanded = expanded,
        onToggle = onToggleEmbedded,
        testTagId = "main_entry_widget_embedded_container"
    ) {
        AndroidView(
            factory = { ctx ->
                viewModel.getEntryWidget().getView(ctx)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("main_entry_widget_embedded_view")
        )
    }
}

@Composable
private fun VisitorCodeSection(
    enabled: Boolean,
    expanded: Boolean,
    onShowDialogClick: () -> Unit,
    onToggleEmbedded: () -> Unit
) {
    ActionButton(
        text = "Show Dialog",
        onClick = onShowDialogClick,
        enabled = enabled,
        testTagId = "main_present_visitor_code_as_alert_button"
    )

    CollapsibleEmbeddedContainer(
        title = "Embedded View",
        expanded = expanded,
        onToggle = onToggleEmbedded,
        testTagId = "main_visitor_code_embedded_container"
    ) {
        AndroidView(
            factory = { ctx ->
                GliaWidgets.getCallVisualizer().createVisitorCodeView(ctx) ?: View(ctx)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("main_visitor_code_embedded_view")
        )
    }
}

// Authentication Dialog
@Composable
fun AuthenticationDialog(
    initialToken: String,
    onDismiss: () -> Unit,
    onAuthenticate: (jwtToken: String, accessToken: String?) -> Unit,
    onClearToken: () -> Unit
) {
    var jwtToken by remember { mutableStateOf(initialToken) }
    var accessToken by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Authentication") },
        text = {
            Column {
                OutlinedTextField(
                    value = jwtToken,
                    onValueChange = { jwtToken = it },
                    label = { Text("JWT Token") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("authentication_id_token_textfield")
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = accessToken,
                    onValueChange = { accessToken = it },
                    label = { Text("Access Token (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("authentication_access_token_textfield")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAuthenticate(
                        jwtToken,
                        accessToken.takeIf { it.isNotBlank() }
                    )
                },
                modifier = Modifier.testTag("create_authentication_alert_button")
            ) {
                Text("Authenticate")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    jwtToken = ""
                    accessToken = ""
                    onClearToken()
                }) {
                    Text("Clear")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_authentication_alert_button")
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

// Deauthentication Dialog
@Composable
fun DeauthenticationDialog(
    onDismiss: () -> Unit,
    onDeauthenticate: (stopPushNotifications: Boolean) -> Unit
) {
    var stopPush by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deauthenticate") },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("main_stop_push_notifications_checkbox")
            ) {
                Checkbox(
                    checked = stopPush,
                    onCheckedChange = { stopPush = it },
                    modifier = Modifier.testTag("deauth_stop_push_checkbox")
                )
                Text("Stop Push Notifications")
            }
        },
        confirmButton = {
            Button(
                onClick = { onDeauthenticate(stopPush) },
                modifier = Modifier.testTag("deauth_confirm_button")
            ) {
                Text("Deauthenticate")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("deauth_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

// Refresh Auth Dialog
@Composable
fun RefreshAuthDialog(
    initialToken: String,
    onDismiss: () -> Unit,
    onRefresh: (jwtToken: String, accessToken: String?) -> Unit,
    onClearToken: () -> Unit
) {
    var jwtToken by remember { mutableStateOf(initialToken) }
    var accessToken by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Refresh Authentication") },
        text = {
            Column {
                OutlinedTextField(
                    value = jwtToken,
                    onValueChange = { jwtToken = it },
                    label = { Text("JWT Token") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("authentication_refresh_token_textfield")
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = accessToken,
                    onValueChange = { accessToken = it },
                    label = { Text("Access Token (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRefresh(jwtToken, accessToken.takeIf { it.isNotBlank() })
                },
                modifier = Modifier.testTag("refresh_token_alert_refresh_button")
            ) {
                Text("Refresh")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    jwtToken = ""
                    accessToken = ""
                    onClearToken()
                }) {
                    Text("Clear")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// Queue Picker Dialog
@Composable
fun QueuePickerDialog(
    queuesState: QueuesState,
    onDismiss: () -> Unit,
    onQueueSelected: (Queue) -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Queue") },
        text = {
            when (queuesState) {
                is QueuesState.Idle -> {
                    Text("Loading...")
                }
                is QueuesState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("queue_picker_loading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is QueuesState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            queuesState.message,
                            modifier = Modifier.testTag("queue_picker_error")
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.testTag("queue_picker_retry_button")
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is QueuesState.Empty -> {
                    Text("No queues available")
                }
                is QueuesState.Loaded -> {
                    Column(modifier = Modifier.testTag("queue_picker_list")) {
                        queuesState.queues.forEachIndexed { index, queue ->
                            ListItem(
                                headlineContent = { Text(queue.name) },
                                supportingContent = { Text("Status: ${queue.status.name}") },
                                modifier = Modifier
                                    .testTag("queue_picker_item_$index")
                            )
                            TextButton(
                                onClick = { onQueueSelected(queue) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Select")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Previews
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenContentPreview() {
    GliaExampleAppTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            GliaLogo(size = 60.dp)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Check, "Configured")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, "Actions")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                DefaultQueuesToggle(
                    useDefaultQueues = false,
                    onToggle = {}
                )
                Spacer(Modifier.height(16.dp))
                SectionHeader("Engagements")
                EngagementButtonsRow(
                    enabled = true,
                    onChatClick = {},
                    onAudioClick = {},
                    onVideoClick = {},
                    onSecureMessagingClick = {}
                )
                Spacer(Modifier.height(16.dp))
                SectionHeader("Authentication")
                ActionButton(text = "Authenticate", onClick = {})
                Spacer(Modifier.height(16.dp))
                SectionHeader("Entry Widget")
                ActionButton(text = "Show Sheet", onClick = {})
                CollapsibleEmbeddedContainer(
                    title = "Embedded View",
                    expanded = false,
                    onToggle = {},
                    testTagId = "preview_entry_widget_embedded"
                ) {
                    Text("Entry Widget Content")
                }
                Spacer(Modifier.height(16.dp))
                SectionHeader("Call Visualizer")
                ActionButton(text = "Show Dialog", onClick = {})
                CollapsibleEmbeddedContainer(
                    title = "Embedded View",
                    expanded = true,
                    onToggle = {},
                    testTagId = "preview_visitor_code_embedded"
                ) {
                    Text("Visitor Code Content")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthenticationDialogPreview() {
    GliaExampleAppTheme {
        AuthenticationDialog(
            initialToken = "sample-jwt-token",
            onDismiss = {},
            onAuthenticate = { _, _ -> },
            onClearToken = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeauthenticationDialogPreview() {
    GliaExampleAppTheme {
        DeauthenticationDialog(
            onDismiss = {},
            onDeauthenticate = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EngagementButtonsRowPreview() {
    GliaExampleAppTheme {
        EngagementButtonsRow(
            enabled = true,
            onChatClick = {},
            onAudioClick = {},
            onVideoClick = {},
            onSecureMessagingClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultQueuesTogglePreview() {
    GliaExampleAppTheme {
        Column {
            DefaultQueuesToggle(useDefaultQueues = false, onToggle = {})
            DefaultQueuesToggle(useDefaultQueues = true, onToggle = {})
        }
    }
}
