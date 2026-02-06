package com.glia.exampleapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.unit.sp
import com.glia.exampleapp.R
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

private val GliaPrimary = Color(0xFF7C19DD)
private val EnabledBackground = GliaPrimary.copy(alpha = 0.1f)
private val EnabledContent = GliaPrimary
private val EnabledBorder = GliaPrimary.copy(alpha = 0.3f)
private val DisabledBackground = Color(0xFFE8E8E8)
private val DisabledText = Color(0xFFAAAAAA)
private val DisabledBorder = Color(0xFFAAAAAA).copy(alpha = 0.3f)

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
    val backgroundColor = if (enabled) EnabledBackground else DisabledBackground
    val contentColor = if (enabled) EnabledContent else DisabledText
    val borderColor = if (enabled) EnabledBorder else DisabledBorder

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = 44.dp)
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = contentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
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
private fun ActionButtonWithIconPreview() {
    GliaExampleAppTheme {
        Column {
            ActionButton(
                text = "Authenticate",
                onClick = {},
                iconRes = R.drawable.ic_key,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
            ActionButton(
                text = "Refresh Token",
                onClick = {},
                iconRes = R.drawable.ic_refresh,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonNoIconPreview() {
    GliaExampleAppTheme {
        ActionButton(
            text = "Show Sheet",
            onClick = {},
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
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
            enabled = false,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}
