package com.glia.exampleapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

/**
 * Standard outlined button for actions like Authenticate, Refresh Token, etc.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTagId: String? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonPreview() {
    GliaExampleAppTheme {
        ActionButton(
            text = "Authenticate",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonDisabledPreview() {
    GliaExampleAppTheme {
        ActionButton(
            text = "Authenticate",
            onClick = {},
            enabled = false
        )
    }
}
