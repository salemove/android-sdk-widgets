package com.glia.widgets.liveobservation

import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.ChatActivity
import org.junit.Test

class ChatActivitySnackBarTest : SnapshotTest() {
    private val helper: SnackBarTestHelper<ChatActivity> by lazy {
        SnackBarTestHelper(context, ChatActivity::class)
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
