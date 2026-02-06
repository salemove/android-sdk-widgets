package com.glia.exampleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glia.exampleapp.data.model.PredefinedColor
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

/**
 * Dropdown for selecting from predefined color palette.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDropdown(
    label: String,
    selectedColor: PredefinedColor,
    onColorSelected: (PredefinedColor) -> Unit,
    modifier: Modifier = Modifier,
    testTagId: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
    ) {
        OutlinedTextField(
            value = selectedColor.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Color preview box
                    if (selectedColor != PredefinedColor.DEFAULT) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(selectedColor.color, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PredefinedColor.entries.forEach { color ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (color != PredefinedColor.DEFAULT) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(color.color, RoundedCornerShape(4.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                )
                                Spacer(Modifier.width(8.dp))
                            }
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

@Preview(showBackground = true)
@Composable
private fun ColorPickerDropdownDefaultPreview() {
    GliaExampleAppTheme {
        var selectedColor by remember { mutableStateOf(PredefinedColor.DEFAULT) }
        ColorPickerDropdown(
            label = "Primary Color",
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerDropdownSelectedPreview() {
    GliaExampleAppTheme {
        var selectedColor by remember { mutableStateOf(PredefinedColor.BLUE) }
        ColorPickerDropdown(
            label = "Primary Color",
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it }
        )
    }
}
