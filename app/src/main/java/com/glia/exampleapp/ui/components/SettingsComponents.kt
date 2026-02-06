package com.glia.exampleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glia.exampleapp.data.model.PredefinedColor
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

// Colors
private val SectionHeaderColor = Color(0xFF8E8E93)
private val CardBackgroundColor = Color.White
private val DividerColor = Color(0xFFE5E5EA)
private val PlaceholderColor = Color(0xFFC7C7CC)
private val BlueTextColor = Color(0xFF007AFF)
private val GreenSwitchColor = Color(0xFF34C759)
private val SegmentedControlBackground = Color(0xFFE5E5EA)
private val InsetTextFieldBackground = Color(0xFFF2F2F7)

/**
 * Rounded inset text field (like iOS text field with gray background)
 * Used for Custom URL field in Environment section
 */
@Composable
fun RoundedInsetTextField(
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
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(InsetTextFieldBackground)
            .padding(horizontal = 12.dp, vertical = 11.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
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

/**
 * Section header text displayed outside cards
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        color = SectionHeaderColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp)
    )
}

/**
 * Card container for settings sections
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Text field for settings with placeholder
 */
@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
    testTagId: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 17.sp,
                color = Color.Black
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
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
        if (trailingContent != null) {
            Spacer(Modifier.width(8.dp))
            trailingContent()
        }
    }
}

/**
 * Toggle row with title and switch
 */
@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    testTagId: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                color = Color.Black
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = SectionHeaderColor
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GreenSwitchColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5EA)
            ),
            modifier = testTagId?.let { Modifier.testTag(it) } ?: Modifier
        )
    }
}

/**
 * Dropdown row for selecting from options
 */
@Composable
fun SettingsDropdownRow(
    title: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagId: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                color = Color.Black
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = testTagId?.let { Modifier.testTag(it) } ?: Modifier
            ) {
                Text(
                    text = selectedValue,
                    color = BlueTextColor,
                    fontSize = 17.sp
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = BlueTextColor
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Segmented control for selecting between options
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagId: String? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SegmentedControlBackground)
            .padding(2.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
    ) {
        options.forEachIndexed { index, option ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (index == selectedIndex) Color.White else Color.Transparent)
                    .clickable { onSelectionChange(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (index == selectedIndex) FontWeight.Medium else FontWeight.Normal,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Color picker row with circle indicator
 */
@Composable
fun ColorPickerRow(
    label: String,
    selectedColor: PredefinedColor,
    onColorSelected: (PredefinedColor) -> Unit,
    modifier: Modifier = Modifier,
    testTagId: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 17.sp,
                color = Color.Black
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .then(
                        if (selectedColor == PredefinedColor.DEFAULT || selectedColor.color == Color.Transparent) {
                            Modifier.border(2.dp, Color.LightGray, CircleShape)
                        } else {
                            Modifier
                                .background(selectedColor.color)
                                .border(1.dp, Color.LightGray, CircleShape)
                        }
                    )
                    .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PredefinedColor.entries.forEach { color ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (color == PredefinedColor.DEFAULT) Color.Transparent else color.color)
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(color.displayName)
                        }
                    },
                    onClick = {
                        onColorSelected(color)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Read-only info row for displaying values
 */
@Composable
fun SettingsInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    testTagId: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 17.sp,
            color = SectionHeaderColor,
            modifier = testTagId?.let { Modifier.testTag(it) } ?: Modifier
        )
    }
}

/**
 * Divider for separating rows within a card
 */
@Composable
fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 16.dp),
        color = DividerColor,
        thickness = 0.5.dp
    )
}

// Previews
@Preview(showBackground = true)
@Composable
private fun SettingsSectionHeaderPreview() {
    GliaExampleAppTheme {
        Column {
            SettingsSectionHeader("Environment")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsCardPreview() {
    GliaExampleAppTheme {
        SettingsCard {
            SettingsTextField(
                value = "",
                onValueChange = {},
                placeholder = "Site ID"
            )
            SettingsDivider()
            SettingsTextField(
                value = "my-api-key",
                onValueChange = {},
                placeholder = "API Key ID"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsToggleRowPreview() {
    GliaExampleAppTheme {
        Column {
            SettingsToggleRow(
                title = "Auto-configure Before Engagement",
                checked = true,
                onCheckedChange = {}
            )
            SettingsToggleRow(
                title = "Present Bubble Overlay",
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedControlPreview() {
    GliaExampleAppTheme {
        Column {
            SegmentedControl(
                options = listOf("Beta", "US", "EU", "Custom"),
                selectedIndex = 0,
                onSelectionChange = {}
            )
            Spacer(Modifier.height(16.dp))
            SegmentedControl(
                options = listOf("Forbidden", "Allowed"),
                selectedIndex = 1,
                onSelectionChange = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerRowPreview() {
    GliaExampleAppTheme {
        Column {
            ColorPickerRow(
                label = "Primary",
                selectedColor = PredefinedColor.BLUE,
                onColorSelected = {}
            )
            ColorPickerRow(
                label = "Secondary",
                selectedColor = PredefinedColor.DEFAULT,
                onColorSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsInfoRowPreview() {
    GliaExampleAppTheme {
        SettingsInfoRow(
            label = "Widgets SDK Version",
            value = "3.0.0"
        )
    }
}
