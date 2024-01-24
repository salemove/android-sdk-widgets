package com.glia.widgets.liveobservation

import android.app.Activity
import com.glia.widgets.SnapshotTest
import com.glia.widgets.snapshotutils.SnapshotSnackBar
import org.junit.Test

class CommonSnackBarTest : SnapshotTest(), SnapshotSnackBar {

    @Test
    fun withDefaultTheme() {
        snapshot(
            setupView(Activity::class)
        )
    }

    @Test
    fun withUnifiedTheme() {
        snapshot(
            setupView(Activity::class, unifiedTheme())
        )
    }

    @Test
    fun withGlobalColors() {
        snapshot(
            setupView(Activity::class, unifiedThemeWithGlobalColors())
        )
    }

}
