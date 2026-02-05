package com.glia.exampleapp.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.glia.exampleapp.data.PushPermissionState
import com.glia.exampleapp.data.SdkVersionInfo
import com.glia.exampleapp.data.model.EnvironmentSelection
import com.glia.exampleapp.data.model.PredefinedColor
import com.glia.exampleapp.data.model.ThemeColors
import com.glia.exampleapp.ui.components.ColorPickerRow
import com.glia.exampleapp.ui.components.RoundedInsetTextField
import com.glia.exampleapp.ui.components.SegmentedControl
import com.glia.exampleapp.ui.components.SettingsCard
import com.glia.exampleapp.ui.components.SettingsDivider
import com.glia.exampleapp.ui.components.SettingsDropdownRow
import com.glia.exampleapp.ui.components.SettingsInfoRow
import com.glia.exampleapp.ui.components.SettingsSectionHeader
import com.glia.exampleapp.ui.components.SettingsTextField
import com.glia.exampleapp.ui.components.SettingsToggleRow
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

// Colors
private val ScreenBackgroundColor = Color(0xFFF2F2F7)
private val CancelButtonColor = Color(0xFFE5E5EA)
private val DoneButtonColor = Color(0xFF1C1C1E)
private val BlueTextColor = Color(0xFF007AFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onRequestPushPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showQueuePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // Cancel button (pill-shaped)
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CancelButtonColor)
                            .clickable { onNavigateBack() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("settings_cancel_button")
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Settings",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    // Done button (pill-shaped, dark)
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DoneButtonColor)
                            .clickable {
                                viewModel.saveConfiguration()
                                onNavigateBack()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("screen_settings_barButtonItem_done")
                    ) {
                        Text(
                            text = "Done",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        },
        containerColor = ScreenBackgroundColor
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                // Environment Section
                SettingsSectionHeader("Environment")
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SegmentedControl(
                            options = listOf("Beta", "US", "EU", "Custom"),
                            selectedIndex = uiState.environment.ordinal,
                            onSelectionChange = { index ->
                                viewModel.updateEnvironment(EnvironmentSelection.entries[index])
                            },
                            testTagId = "settings_environment_picker"
                        )
                    }
                    if (uiState.environment == EnvironmentSelection.CUSTOM) {
                        SettingsDivider()
                        RoundedInsetTextField(
                            value = uiState.customEnvironmentUrl,
                            onValueChange = { viewModel.updateCustomEnvironmentUrl(it) },
                            placeholder = "Custom URL",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            testTagId = "settings_custom_environment_url_textfield"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Glia Configuration Section
                SettingsSectionHeader("Glia Configuration")
                SettingsCard {
                    SettingsTextField(
                        value = uiState.siteId,
                        onValueChange = { viewModel.updateSiteId(it) },
                        placeholder = "Site ID",
                        testTagId = "settings_siteId_textfield"
                    )
                    SettingsDivider()
                    SettingsTextField(
                        value = uiState.apiKeyId,
                        onValueChange = { viewModel.updateApiKeyId(it) },
                        placeholder = "API Key ID",
                        testTagId = "settings_apiKeyId_textfield"
                    )
                    SettingsDivider()
                    SettingsTextField(
                        value = uiState.apiKeySecret,
                        onValueChange = { viewModel.updateApiKeySecret(it) },
                        placeholder = "API Key Secret",
                        isPassword = true,
                        testTagId = "settings_apiKeySecret_textfield"
                    )
                    SettingsDivider()
                    SettingsTextField(
                        value = uiState.queueId,
                        onValueChange = { viewModel.updateQueueId(it) },
                        placeholder = "Queue ID",
                        testTagId = "settings_queueId_textfield",
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    viewModel.loadQueues()
                                    showQueuePicker = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Select Queue",
                                    tint = BlueTextColor
                                )
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsTextField(
                        value = uiState.visitorContextAssetId,
                        onValueChange = { viewModel.updateVisitorContextAssetId(it) },
                        placeholder = "Visitor Context Asset ID",
                        testTagId = "settings_visitor_context_assetId_textfield"
                    )
                    SettingsDivider()
                    SettingsTextField(
                        value = uiState.manualLocaleOverride,
                        onValueChange = { viewModel.updateManualLocaleOverride(it) },
                        placeholder = "Manual Locale Override",
                        testTagId = "settings_manual_locale_override_textfield"
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Auto-configure Before Engagement",
                        checked = uiState.autoConfigureEnabled,
                        onCheckedChange = { viewModel.updateAutoConfigureEnabled(it) },
                        testTagId = "settings_auto_configure_switch"
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Authentication Section
                SettingsSectionHeader("Authentication")
                SettingsCard {
                    SettingsDropdownRow(
                        title = "Behavior During Engagement",
                        selectedValue = if (uiState.authenticationBehaviorAllowed) "Allowed" else "Forbidden",
                        options = listOf("Forbidden", "Allowed"),
                        onOptionSelected = { value ->
                            viewModel.updateAuthenticationBehaviorAllowed(value == "Allowed")
                        },
                        testTagId = "settings_authentication_behavior_picker"
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Suppress Push Permission",
                        checked = uiState.suppressPushNotificationDialog,
                        onCheckedChange = { viewModel.updateSuppressPushPermission(it) },
                        testTagId = "settings_suppress_push_permission_switch"
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Features Section
                SettingsSectionHeader("Features")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Present Bubble Overlay During Engagement",
                        checked = uiState.enableBubbleInsideApp,
                        onCheckedChange = { viewModel.updateBubbleInsideApp(it) },
                        testTagId = "settings_bubble_inside_app_switch"
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Theme Colors Section
                SettingsSectionHeader("Theme Colors")
                SettingsCard {
                    ColorPickerRow(
                        label = "Primary",
                        selectedColor = uiState.themeColors.primary,
                        onColorSelected = { viewModel.updatePrimaryColor(it) },
                        testTagId = "settings_primary_color_picker"
                    )
                    SettingsDivider()
                    ColorPickerRow(
                        label = "Secondary",
                        selectedColor = uiState.themeColors.secondary,
                        onColorSelected = { viewModel.updateSecondaryColor(it) },
                        testTagId = "settings_secondary_color_picker"
                    )
                    SettingsDivider()
                    ColorPickerRow(
                        label = "Base Normal",
                        selectedColor = uiState.themeColors.baseNormal,
                        onColorSelected = { viewModel.updateBaseNormalColor(it) },
                        testTagId = "settings_base_normal_color_picker"
                    )
                    SettingsDivider()
                    ColorPickerRow(
                        label = "Base Light",
                        selectedColor = uiState.themeColors.baseLight,
                        onColorSelected = { viewModel.updateBaseLightColor(it) },
                        testTagId = "settings_base_light_color_picker"
                    )
                    SettingsDivider()
                    ColorPickerRow(
                        label = "Base Dark",
                        selectedColor = uiState.themeColors.baseDark,
                        onColorSelected = { viewModel.updateBaseDarkColor(it) },
                        testTagId = "settings_base_dark_color_picker"
                    )
                    SettingsDivider()
                    ColorPickerRow(
                        label = "Base Shade",
                        selectedColor = uiState.themeColors.baseShade,
                        onColorSelected = { viewModel.updateBaseShadeColor(it) },
                        testTagId = "settings_base_shade_color_picker"
                    )
                    SettingsDivider()
                    ColorPickerRow(
                        label = "System Negative",
                        selectedColor = uiState.themeColors.systemNegative,
                        onColorSelected = { viewModel.updateSystemNegativeColor(it) },
                        testTagId = "settings_system_negative_color_picker"
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Application Section
                SettingsSectionHeader("Application")
                SettingsCard {
                    SettingsInfoRow(
                        label = "Push Notification Status",
                        value = when (uiState.pushPermissionState) {
                            PushPermissionState.GRANTED -> "Granted"
                            PushPermissionState.NOT_GRANTED -> "Not Granted"
                            PushPermissionState.NOT_REQUIRED -> "Not Required (< Android 13)"
                        },
                        testTagId = "settings_push_notification_status"
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        uiState.pushPermissionState == PushPermissionState.NOT_GRANTED) {
                        SettingsDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRequestPushPermission() }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("settings_request_push_permission_button"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Request Push Permission",
                                fontSize = 17.sp,
                                color = BlueTextColor
                            )
                        }
                    }
                    SettingsDivider()
                    SettingsInfoRow(
                        label = "Widgets SDK Version",
                        value = uiState.sdkVersionInfo.widgetsSdkVersion.ifEmpty { "N/A" },
                        testTagId = "settings_widgets_sdk_version"
                    )
                    SettingsDivider()
                    SettingsInfoRow(
                        label = "Core SDK Version",
                        value = uiState.sdkVersionInfo.coreSdkVersion.ifEmpty { "N/A" },
                        testTagId = "settings_core_sdk_version"
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Queue Picker Dialog
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
private fun QueuePickerDialog(
    queuesState: SettingsQueuesState,
    onDismiss: () -> Unit,
    onQueueSelected: (com.glia.widgets.queue.Queue) -> Unit,
    onRetry: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        SettingsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Queue",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when (queuesState) {
                    is SettingsQueuesState.Idle,
                    is SettingsQueuesState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                    is SettingsQueuesState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(queuesState.message)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onRetry) {
                                Text("Retry")
                            }
                        }
                    }
                    is SettingsQueuesState.Empty -> {
                        Text("No queues available")
                    }
                    is SettingsQueuesState.Loaded -> {
                        Column {
                            queuesState.queues.forEach { queue ->
                                ListItem(
                                    headlineContent = { Text(queue.name) },
                                    supportingContent = { Text("Status: ${queue.status.name}") },
                                    modifier = Modifier.clickable { onQueueSelected(queue) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    GliaExampleAppTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CancelButtonColor)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    },
                    title = {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Settings", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DoneButtonColor)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Done", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                )
            },
            containerColor = ScreenBackgroundColor
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                SettingsSectionHeader("Environment")
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SegmentedControl(
                            options = listOf("Beta", "US", "EU", "Custom"),
                            selectedIndex = 0,
                            onSelectionChange = {}
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                SettingsSectionHeader("Glia Configuration")
                SettingsCard {
                    SettingsTextField(value = "", onValueChange = {}, placeholder = "Site ID")
                    SettingsDivider()
                    SettingsTextField(value = "", onValueChange = {}, placeholder = "API Key ID")
                    SettingsDivider()
                    SettingsTextField(value = "", onValueChange = {}, placeholder = "API Key Secret", isPassword = true)
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Auto-configure Before Engagement",
                        checked = true,
                        onCheckedChange = {}
                    )
                }

                Spacer(Modifier.height(16.dp))

                SettingsSectionHeader("Theme Colors")
                SettingsCard {
                    ColorPickerRow(
                        label = "Primary",
                        selectedColor = PredefinedColor.BLUE,
                        onColorSelected = {}
                    )
                    SettingsDivider()
                    ColorPickerRow(
                        label = "Secondary",
                        selectedColor = PredefinedColor.DEFAULT,
                        onColorSelected = {}
                    )
                }

                Spacer(Modifier.height(16.dp))

                SettingsSectionHeader("Application")
                SettingsCard {
                    SettingsInfoRow(label = "Widgets SDK Version", value = "3.0.0")
                    SettingsDivider()
                    SettingsInfoRow(label = "Core SDK Version", value = "2.0.0")
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
