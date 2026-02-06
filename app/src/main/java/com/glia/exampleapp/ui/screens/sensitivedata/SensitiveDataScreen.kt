package com.glia.exampleapp.ui.screens.sensitivedata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glia.widgets.GliaWidgets
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

/**
 * Sensitive Data screen that demonstrates Live Observation pause/resume functionality.
 * This screen pauses Live Observation when shown and resumes it when closed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensitiveDataScreen(
    onNavigateBack: () -> Unit
) {
    // Pause Live Observation when this screen is shown
    // Resume when it's disposed
    DisposableEffect(Unit) {
        try {
            GliaWidgets.getLiveObservation().pause()
        } catch (e: Exception) {
            // SDK may not be initialized - ignore
        }

        onDispose {
            try {
                GliaWidgets.getLiveObservation().resume()
            } catch (e: Exception) {
                // SDK may not be initialized - ignore
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sensitive Data")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sensitiveData_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This screen emulates the integrator application screen with sensitive data.\n\n" +
                        "Be aware this screen should not be visible to the operator during Live Observation, but " +
                        "should be visible during screen sharing.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("sensitiveData_message")
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SensitiveDataScreenPreview() {
    GliaExampleAppTheme {
        SensitiveDataScreen(onNavigateBack = {})
    }
}
