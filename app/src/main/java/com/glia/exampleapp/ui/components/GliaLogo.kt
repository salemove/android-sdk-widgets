package com.glia.exampleapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glia.exampleapp.R
import com.glia.exampleapp.ui.theme.GliaExampleAppTheme

/**
 * Glia logo component for use in the app bar and other places.
 */
@Composable
fun GliaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_glia_logo),
        contentDescription = "Glia Logo",
        modifier = modifier.size(width = size, height = size * 18 / 40)
    )
}

@Preview(showBackground = true)
@Composable
private fun GliaLogoPreview() {
    GliaExampleAppTheme {
        GliaLogo()
    }
}

@Preview(showBackground = true)
@Composable
private fun GliaLogoLargePreview() {
    GliaExampleAppTheme {
        GliaLogo(size = 80.dp)
    }
}
