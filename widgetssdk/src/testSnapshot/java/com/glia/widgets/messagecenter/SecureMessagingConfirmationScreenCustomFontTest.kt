package com.glia.widgets.messagecenter

import android.graphics.Typeface
import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotMessageCenterView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingConfirmationScreenTheme
import org.junit.Test

/**
 * The confirmation screen restyles the app bar title in code, and its own title style names a font
 * of its own. Both keep the theme font only when code applies it again, before the JSON theme.
 */
internal class SecureMessagingConfirmationScreenCustomFontTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL,
    theme = "Test_Glia_CustomFont"
), SnapshotMessageCenterView {

    @Test
    fun confirmation() {
        snapshot(setupView(callback = { it.showConfirmationScreen() }).view)
    }

    @Test
    fun confirmationWithRegularTitleFromJson() {
        val unifiedTheme = UnifiedTheme(
            secureMessagingConfirmationScreenTheme = SecureMessagingConfirmationScreenTheme(
                titleTheme = TextTheme(textStyle = Typeface.NORMAL)
            )
        )
        snapshot(setupView(unifiedTheme = unifiedTheme, callback = { it.showConfirmationScreen() }).view)
    }
}
