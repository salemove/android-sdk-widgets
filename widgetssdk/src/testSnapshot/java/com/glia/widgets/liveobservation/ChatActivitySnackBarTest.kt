package com.glia.widgets.liveobservation

import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.snapshotutils.SnapshotSnackBar
import org.junit.Test

internal class ChatActivitySnackBarTest : SnapshotTest(), SnapshotSnackBar {

    @Test
    fun withDefaultTheme() {
        snapshot(
            setupView(ChatActivity::class)
        )
    }

    @Test
    fun withUnifiedTheme() {
        snapshot(
            setupView(ChatActivity::class, unifiedTheme())
        )
    }

    @Test
    fun withGlobalColors() {
        snapshot(
            setupView(ChatActivity::class, unifiedThemeWithGlobalColors())
        )
    }

}
