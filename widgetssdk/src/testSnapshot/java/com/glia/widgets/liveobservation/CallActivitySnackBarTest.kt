package com.glia.widgets.liveobservation

import com.glia.widgets.SnapshotTest
import com.glia.widgets.call.CallActivity
import com.glia.widgets.snapshotutils.SnapshotSnackBar
import org.junit.Test

class CallActivitySnackBarTest : SnapshotTest(), SnapshotSnackBar {

    @Test
    fun withDefaultTheme() {
        snapshot(
            setupView(CallActivity::class)
        )
    }

    @Test
    fun withUnifiedTheme() {
        snapshot(
            setupView(CallActivity::class, unifiedTheme())
        )
    }

    @Test
    fun withGlobalColors() {
        snapshot(
            setupView(CallActivity::class, unifiedThemeWithGlobalColors())
        )
    }

}
