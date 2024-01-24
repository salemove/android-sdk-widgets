package com.glia.widgets.chat

import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.snapshotutils.SnapshotChatView
import com.glia.widgets.snapshotutils.SnapshotGva
import org.junit.Test

internal class ChatViewQuickReplySnapshotTest : SnapshotTest(), SnapshotChatView, SnapshotGva {

    private fun gvaButtons() = shortLengthTexts().map { GvaButton(it) }

    private fun chatState() = ChatState()
        .changeVisibility(true)
        .copy(gvaQuickReplies = gvaButtons())

    @Test
    fun defaultTheme() {
        snapshot(
            setupView(
                chatState = chatState()
            ).root
        )
    }

    @Test
    fun withUiTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun withGlobalColors() {
        snapshot(
            setupView(
                chatState = chatState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun withWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun withWithUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                chatState = chatState(),
                unifiedTheme = unifiedThemeWithoutGva()
            ).root
        )
    }
}
