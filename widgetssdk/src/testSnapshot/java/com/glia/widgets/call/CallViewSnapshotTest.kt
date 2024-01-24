package com.glia.widgets.call

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import org.junit.Test

internal class CallViewSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotCallView {

    @Test
    fun initialState() {
        snapshot(
            setupView(
                callState = callState()
            ).root
        )
    }
}
