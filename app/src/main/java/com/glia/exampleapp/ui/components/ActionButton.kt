package com.glia.exampleapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glia.exampleapp.R
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

// Light purple background color
private val LightPurpleBackground = Color(0xFFF0EBFF)
private val PurpleText = Color(0xFF6B4EFF)
private val DisabledBackground = Color(0xFFE8E8E8)
private val DisabledText = Color(0xFFAAAAAA)

/**
 * Light purple button with optional icon for actions like Authenticate, Show Sheet, etc.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    enabled: Boolean = true,
    testTagId: String? = null
) {
    val backgroundColor = if (enabled) LightPurpleBackground else DisabledBackground
    val contentColor = if (enabled) PurpleText else DisabledText

    // Apply padding outside the clip for proper spacing
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Full-width action button variant with horizontal padding built-in.
 * Use this for standalone buttons, not in Rows with weight.
 */
@Composable
fun FullWidthActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    enabled: Boolean = true,
    testTagId: String? = null
) {
    ActionButton(
        text = text,
        onClick = onClick,
        iconRes = iconRes,
        enabled = enabled,
        testTagId = testTagId,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonPreview() {
    GliaExampleAppTheme {
        ActionButton(
            text = "Authenticate",
            onClick = {},
            iconRes = R.drawable.ic_key
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonNoIconPreview() {
    GliaExampleAppTheme {
        ActionButton(
            text = "Show Sheet",
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
            iconRes = R.drawable.ic_key,
            enabled = false
        )
    }
}
