package com.glia.widgets.messagecenter

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotMessageCenterView
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class SecureMessagingConfirmationScreenTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotMessageCenterView, SnapshotStrings {

    private fun confirmationView(unifiedTheme: UnifiedTheme? = null) = setupView(
        callback = {
            it.showConfirmationScreen()
        },
        unifiedTheme = unifiedTheme
    ).view

    @Test
    fun confirmation() {
        snapshot(
            confirmationView()
        )
    }

    @Test
    fun confirmationWithGlobalColors() {
        snapshot(
            confirmationView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun confirmationWithUnifiedTheme() {
        snapshot(
            confirmationView(
                unifiedTheme = unifiedTheme()
            )
        )
    }

    @Test
    fun confirmationWithUnifiedThemeWithoutConfirmationScreen() {
        snapshot(
            confirmationView(
                unifiedTheme = unifiedThemeWithoutConfirmationScreen()
            )
        )
    }

}
