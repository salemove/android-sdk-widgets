package com.glia.exampleapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

/**
 * Banner that appears when settings requiring restart are changed.
 */
@Composable
fun RestartWarningBanner(
    visible: Boolean,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings changed. Restart required.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onRestartClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("settings_restart_button")
                ) {
                    Text("Restart")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestartWarningBannerVisiblePreview() {
    GliaExampleAppTheme {
        RestartWarningBanner(
            visible = true,
            onRestartClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestartWarningBannerHiddenPreview() {
    GliaExampleAppTheme {
        RestartWarningBanner(
            visible = false,
            onRestartClick = {}
        )
    }
}
