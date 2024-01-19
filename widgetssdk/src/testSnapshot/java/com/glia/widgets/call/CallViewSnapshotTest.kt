package com.glia.widgets.call

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import org.junit.Test

internal class CallViewSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotCallView {

    override val callViewMock = SnapshotCallView.Mock(this)

    override fun setUp() {
        super.setUp()
        callViewMock.setUp()
    }

    override fun tearDown() {
        super.tearDown()
        callViewMock.tearDown()
    }

    @Test
    fun initialState() {
        snapshot(
            setupView(
                callState = callState()
            ).root
        )
    }
}
