package com.glia.widgets.liveobservation

import android.app.Activity
import com.glia.widgets.SnapshotTest
import org.junit.Test

class CommonSnackBarTest : SnapshotTest() {
    private val helper: SnackBarTestHelper<Activity> by lazy {
        SnackBarTestHelper(context, Activity::class)
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
