package com.glia.widgets.messagecenter

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotMessageCenterView
import org.junit.Test

/**
 * Both secure messaging screens paint their own background, so they follow
 * `gliaChatBackgroundColor` only when that code reads it from the theme.
 */
internal class SecureMessagingCustomizedThemeSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL,
    theme = "Test_Glia_Customized"
), SnapshotMessageCenterView {

    @Test
    fun welcome() {
        snapshot(setupView().view)
    }

    @Test
    fun confirmation() {
        snapshot(setupView(callback = { it.showConfirmationScreen() }).view)
    }
}
