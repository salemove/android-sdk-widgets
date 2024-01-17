package com.glia.widgets.call

import com.glia.widgets.SnapshotTest
import org.junit.Test

internal class CallViewSnapshotTest : SnapshotTest(), SnapshotCallView {

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
        snapshotFullSize(
            setupView(
                callState = callState()
            ).root
        )
    }
}
