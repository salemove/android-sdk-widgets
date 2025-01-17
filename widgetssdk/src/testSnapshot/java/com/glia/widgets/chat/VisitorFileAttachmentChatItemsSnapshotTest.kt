package com.glia.widgets.chat

import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.snapshotutils.SnapshotAttachment
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotChatView
import org.junit.Test

internal class VisitorFileAttachmentChatItemsSnapshotTest : SnapshotTest(), SnapshotChatView, SnapshotChatScreen, SnapshotAttachment {

    private fun chatState() = ChatState()
        .changeVisibility(true)

    // MARK: Visitor File without labels

    @Test
    fun defaultTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile())
            ).root
        )
    }

    @Test
    fun withoutLabelsWithGlobalColors() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile()),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun withoutLabelsWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile()),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun withoutLabelsWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile()),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).root
        )
    }

}
