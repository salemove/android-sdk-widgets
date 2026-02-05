package com.glia.exampleapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
private val PurpleContent = Color(0xFF6B4EFF)

/**
 * Container that can collapse/expand by clicking header.
 * Used for embedded Entry Widget and Visitor Code views.
 * Matches iOS design with chevron icon and light purple background.
 */
@Composable
fun CollapsibleEmbeddedContainer(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    testTagId: String? = null,
    content: @Composable () -> Unit
) {
    // Animate chevron rotation
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "chevron_rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(testTagId?.let { Modifier.testTag(it) } ?: Modifier)
    ) {
        // Clickable header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LightPurpleBackground)
                .clickable(onClick = onToggle)
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = PurpleContent,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotationAngle)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = PurpleContent,
                fontWeight = FontWeight.Medium
            )
        }

        // Animated content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollapsibleEmbeddedContainerExpandedPreview() {
    GliaExampleAppTheme {
        var expanded by remember { mutableStateOf(true) }
        CollapsibleEmbeddedContainer(
            title = "Embedded View",
            expanded = expanded,
            onToggle = { expanded = !expanded }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Content goes here")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollapsibleEmbeddedContainerCollapsedPreview() {
    GliaExampleAppTheme {
        CollapsibleEmbeddedContainer(
            title = "Embedded View",
            expanded = false,
            onToggle = {}
        ) {
            Text("Content goes here")
        }
    }
}
