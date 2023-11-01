package com.glia.widgets.liveobservation

import com.glia.widgets.SnapshotTest
import com.glia.widgets.call.CallActivity
import org.junit.Test

class CallActivitySnackBarTest : SnapshotTest() {
    private val helper: SnackBarTestHelper<CallActivity> by lazy {
        SnackBarTestHelper(context, CallActivity::class)
    }

    override fun setUp() {
        helper.setUp()
    }

    override fun tearDown() {
        helper.tearDown()
    }

    @Test
    fun withDefaultTheme() {
        snapshot(helper.getView(null))
    }

    @Test
    fun withUnifiedTheme() {
        snapshot(helper.getView(unifiedTheme()))
    }

    @Test
    fun withGlobalColors() {
        snapshot(helper.getView(unifiedThemeWithGlobalColors()))
    }

}
