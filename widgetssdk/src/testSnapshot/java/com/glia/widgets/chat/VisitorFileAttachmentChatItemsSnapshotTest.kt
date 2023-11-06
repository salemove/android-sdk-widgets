package com.glia.widgets.chat

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.model.Attachment
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotChatView
import org.junit.Test

internal class VisitorFileAttachmentChatItemsSnapshotTest : SnapshotTest(), SnapshotChatView, SnapshotChatScreen {

    override val chatViewMock = SnapshotChatView.Mock(this)

    override fun setUp() {
        super.setUp()
        chatViewMock.setUp()
    }

    override fun tearDown() {
        chatViewMock.tearDown()
        super.tearDown()
    }

    private fun remoteAttachment() = Attachment.Remote(
        object : AttachmentFile {
            override fun getId(): String = "fileId"

            override fun getSize(): Long = 1234567890

            override fun getContentType(): String = "pdf"

            override fun isDeleted(): Boolean = false

            override fun getName(): String ="File Name.pdf"
        }
    )

    private fun file(
        attachment: Attachment = remoteAttachment(),
        showDelivered: Boolean = false,
        showError: Boolean = false
    ) = VisitorAttachmentItem.File(
        id = "fileId",
        attachment = attachment,
        showDelivered = showDelivered,
        showError = showError
    )

    private fun chatState() = ChatState()
        .changeVisibility(true)

    // MARK: Visitor File without labels

    @Test
    fun defaultTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file())
            ).root
        )
    }

    @Test
    fun withoutLabelsWithUiTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file()),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun withoutLabelsWithGlobalColors() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file()),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun withoutLabelsWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file()),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun withoutLabelsWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file()),
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
                chatItems = listOf(file(showDelivered = true))
            ).root
        )
    }

    @Test
    fun deliveredLabelWithUiTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showDelivered = true)),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun deliveredLabelWithGlobalColors() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showDelivered = true)),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun deliveredLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showDelivered = true)),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun deliveredLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showDelivered = true)),
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
                chatItems = listOf(file(showError = true))
            ).root
        )
    }

    @Test
    fun errorLabelWithUiTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showError = true)),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun errorLabelWithGlobalColors() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showError = true)),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun errorLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showError = true)),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun errorLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                chatState = chatState(),
                chatItems = listOf(file(showError = true)),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).root
        )
    }

}
