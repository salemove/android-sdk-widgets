package com.glia.exampleapp.ui.screens.visitorinfo

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glia.exampleapp.ui.components.SegmentedControl
import com.glia.exampleapp.ui.components.SettingsCard
import com.glia.exampleapp.ui.components.SettingsDivider
import com.glia.exampleapp.ui.components.SettingsSectionHeader
import com.glia.exampleapp.ui.components.SettingsTextField
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

// Colors
private val ScreenBackgroundColor = Color(0xFFF2F2F7)
private val BlueTextColor = Color(0xFF007AFF)
private val PlaceholderColor = Color(0xFFC7C7CC)
private val DisabledTextColor = Color(0xFFC7C7CC)
private val SectionHeaderColor = Color(0xFF8E8E93)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorInfoScreen(
    viewModel: VisitorInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Text(
                        text = "Cancel",
                        color = BlueTextColor,
                        fontSize = 17.sp,
                        modifier = Modifier
                            .clickable { onNavigateBack() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("visitor_info_cancel_button")
                    )
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Visitor Information",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Save",
                        color = if (uiState.hasChanges) BlueTextColor else DisabledTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(enabled = uiState.hasChanges) {
                                viewModel.save { onNavigateBack() }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("visitor_info_save_button")
                    )
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

            // Basic Information Section
            SettingsSectionHeader("Basic Information")
            SettingsCard {
                SettingsTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    placeholder = "Name",
                    testTagId = "visitor_info_name_field"
                )
                SettingsDivider()
                SettingsTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    placeholder = "Email",
                    testTagId = "visitor_info_email_field"
                )
                SettingsDivider()
                SettingsTextField(
                    value = uiState.phone,
                    onValueChange = { viewModel.updatePhone(it) },
                    placeholder = "Phone Number",
                    testTagId = "visitor_info_phone_field"
                )
                SettingsDivider()
                SettingsTextField(
                    value = uiState.externalId,
                    onValueChange = { viewModel.updateExternalId(it) },
                    placeholder = "External ID",
                    testTagId = "visitor_info_external_id_field"
                )
            }

            Spacer(Modifier.height(16.dp))

            // Notes Section
            SectionHeaderWithControl(
                title = "Notes",
                options = listOf("Replace", "Append"),
                selectedIndex = if (uiState.notesMode == NotesMode.REPLACE) 0 else 1,
                onSelectionChange = {
                    viewModel.updateNotesMode(if (it == 0) NotesMode.REPLACE else NotesMode.APPEND)
                },
                testTagId = "visitor_info_notes_mode_toggle"
            )
            SettingsCard {
                MultilineTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    placeholder = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(16.dp),
                    testTagId = "visitor_info_notes_field"
                )
            }

            Spacer(Modifier.height(16.dp))

            // Custom Attributes Section
            SectionHeaderWithControl(
                title = "Custom Attributes",
                options = listOf("Replace", "Merge"),
                selectedIndex = if (uiState.customAttributesMode == CustomAttributesMode.REPLACE) 0 else 1,
                onSelectionChange = {
                    viewModel.updateCustomAttributesMode(if (it == 0) CustomAttributesMode.REPLACE else CustomAttributesMode.MERGE)
                },
                testTagId = "visitor_info_custom_attrs_mode_toggle"
            )
            SettingsCard {
                uiState.customAttributes.forEachIndexed { index, attribute ->
                    CustomAttributeRow(
                        attribute = attribute,
                        onKeyChange = { viewModel.updateCustomAttributeKey(index, it) },
                        onValueChange = { viewModel.updateCustomAttributeValue(index, it) },
                        onRemove = { viewModel.removeCustomAttribute(index) },
                        keyTestTagId = "visitor_info_custom_attribute_key_$index",
                        valueTestTagId = "visitor_info_custom_attribute_value_$index"
                    )
                    SettingsDivider()
                }

                // Add Custom Attribute button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.addCustomAttribute() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("visitor_info_add_attribute_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "Add",
                        tint = BlueTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Add Custom Attribute",
                        color = BlueTextColor,
                        fontSize = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeaderWithControl(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagId: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        SegmentedControl(
            options = options,
            selectedIndex = selectedIndex,
            onSelectionChange = onSelectionChange,
            testTagId = testTagId
        )
    }
}

@Composable
private fun CustomAttributeRow(
    attribute: CustomAttribute,
    onKeyChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    keyTestTagId: String? = null,
    valueTestTagId: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = attribute.key,
                onValueChange = onKeyChange,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 17.sp,
                    color = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(keyTestTagId?.let { Modifier.testTag(it) } ?: Modifier),
                decorationBox = { innerTextField ->
                    Box {
                        if (attribute.key.isEmpty()) {
                            Text(
                                text = "Key",
                                color = PlaceholderColor,
                                fontSize = 17.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(Modifier.height(4.dp))
            BasicTextField(
                value = attribute.value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 15.sp,
                    color = SectionHeaderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(valueTestTagId?.let { Modifier.testTag(it) } ?: Modifier),
                decorationBox = { innerTextField ->
                    Box {
                        if (attribute.value.isEmpty()) {
                            Text(
                                text = "Value",
                                color = PlaceholderColor,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Red
            )
        }
    }
}

@Composable
private fun MultilineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTagId: String? = null
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 17.sp,
            color = Color.Black
        ),
        modifier = modifier.then(testTagId?.let { Modifier.testTag(it) } ?: Modifier),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = PlaceholderColor,
                        fontSize = 17.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VisitorInfoScreenPreview() {
    GliaExampleAppTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    navigationIcon = {
                        Text(
                            text = "Cancel",
                            color = BlueTextColor,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    },
                    title = {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Visitor Information", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    actions = {
                        Text(
                            text = "Save",
                            color = DisabledTextColor,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
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

                SettingsSectionHeader("Basic Information")
                SettingsCard {
                    SettingsTextField(value = "", onValueChange = {}, placeholder = "Name")
                    SettingsDivider()
                    SettingsTextField(value = "", onValueChange = {}, placeholder = "Email")
                    SettingsDivider()
                    SettingsTextField(value = "", onValueChange = {}, placeholder = "Phone Number")
                    SettingsDivider()
                    SettingsTextField(value = "", onValueChange = {}, placeholder = "External ID")
                }

                Spacer(Modifier.height(16.dp))

                SectionHeaderWithControl(
                    title = "Notes",
                    options = listOf("Replace", "Append"),
                    selectedIndex = 0,
                    onSelectionChange = {}
                )
                SettingsCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(16.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                SectionHeaderWithControl(
                    title = "Custom Attributes",
                    options = listOf("Replace", "Merge"),
                    selectedIndex = 0,
                    onSelectionChange = {}
                )
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Add",
                            tint = BlueTextColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Add Custom Attribute", color = BlueTextColor, fontSize = 17.sp)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
