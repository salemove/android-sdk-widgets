package com.glia.exampleapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glia.exampleapp.R
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

private val GradientStart = Color(0xFF3B0091)
private val GradientEnd = Color(0xFF7C19DD)

/**
 * Purple gradient button with icon and text for engagement types (Chat, Audio, Video, Secure Messaging).
 */
@Composable
fun EngagementButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTagId: String? = null
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) gradientBrush else Brush.verticalGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EngagementButtonPreview() {
    GliaExampleAppTheme {
        EngagementButton(
            text = "Chat",
            iconRes = R.drawable.ic_baseline_chat_bubble,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EngagementButtonDisabledPreview() {
    GliaExampleAppTheme {
        EngagementButton(
            text = "Chat",
            iconRes = R.drawable.ic_baseline_chat_bubble,
            onClick = {},
            enabled = false
        )
    }
}
