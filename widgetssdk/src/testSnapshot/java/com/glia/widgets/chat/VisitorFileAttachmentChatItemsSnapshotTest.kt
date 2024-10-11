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

    // MARK: Visitor File with delivered label

    @Test
    fun deliveredLabel() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showDelivered = true))
            ).root
        )
    }

    @Test
    fun deliveredLabelWithGlobalColors() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showDelivered = true)),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun deliveredLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showDelivered = true)),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun deliveredLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showDelivered = true)),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).root
        )
    }

    // MARK: Visitor File with error label

    @Test
    fun errorLabel() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showError = true))
            ).root
        )
    }

    @Test
    fun errorLabelWithGlobalColors() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showError = true)),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun errorLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showError = true)),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun errorLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(visitorAttachmentItemFile(showError = true)),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).root
        )
    }

}
