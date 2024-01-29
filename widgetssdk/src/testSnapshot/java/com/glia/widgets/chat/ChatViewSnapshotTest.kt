package com.glia.widgets.chat

import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotChatView
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class ChatViewSnapshotTest : SnapshotTest(), SnapshotChatView, SnapshotChatScreen, SnapshotStrings {

    @Test
    fun initialState() {
        snapshot(
            setupView(
                chatState = ChatState()
            ).root
        )
    }

    // MARK: Attachment list

    private fun attachmentListView(
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null
    ) = setupView(
        chatState = ChatState()
            .changeVisibility(true)
            .engagementStarted()
            .operatorConnected("Snap Test", null)
            .setShowSendButton(true),
        fileAttachments = listOf(
            fileAttachment(
                displayName = "test.doc",
                status = FileAttachment.Status.READY_TO_SEND
            ),
            fileAttachment(
                displayName = "photo.jpg",
                isImage = true,
                size = 123456,
                status = FileAttachment.Status.SECURITY_SCAN
            ),
            fileAttachment(
                displayName = "image.jpg",
                size = 654321,
                isImage = true
            ),
            fileAttachment(
                displayName = "failed.file",
                status = FileAttachment.Status.ERROR_SECURITY_SCAN_FAILED
            )
        ),
        imageResources = listOf(
            R.drawable.test_banner,
            R.drawable.test_launcher
        ),
        message = mediumLengthTexts()[0],
        unifiedTheme = unifiedTheme,
        uiTheme = uiTheme
    )

    @Test
    fun attachmentList() {
        snapshot(
            attachmentListView().root
        )
    }

    @Test
    fun attachmentListWithUiTheme() {
        snapshot(
            attachmentListView(
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun attachmentListWithGlobalColors() {
        snapshot(
            attachmentListView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun attachmentListWithUnifiedTheme() {
        snapshot(
            attachmentListView(
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun attachmentListWithUnifiedThemeWithoutChat() {
        snapshot(
            attachmentListView(
                unifiedTheme = unifiedThemeWithoutChat()
            ).root
        )
    }

    // MARK: Failed attachment list

    private fun failedAttachmentListView(
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null
    ) = setupView(
        chatState = ChatState()
            .changeVisibility(true)
            .engagementStarted()
            .setIsAttachmentButtonEnabled(false)
            .setShowSendButton(false),
        fileAttachments = listOf(
            fileAttachment(
                displayName = "test.doc",
                status = FileAttachment.Status.ERROR_NETWORK_TIMEOUT
            ),
            fileAttachment(
                displayName = "photo.jpg",
                isImage = true,
                size = 123456,
                status = FileAttachment.Status.ERROR_SECURITY_SCAN_FAILED
            )
        ),
        imageResources = listOf(
            R.drawable.test_banner,
            R.drawable.test_launcher
        ),
        message = mediumLengthTexts()[1],
        unifiedTheme = unifiedTheme,
        uiTheme = uiTheme
    )

    @Test
    fun failedAttachmentList() {
        snapshot(
            failedAttachmentListView().root
        )
    }

    @Test
    fun failedAttachmentListWithUiTheme() {
        snapshot(
            failedAttachmentListView(
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun failedAttachmentListWithGlobalColors() {
        snapshot(
            failedAttachmentListView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun failedAttachmentListWithUnifiedTheme() {
        snapshot(
            failedAttachmentListView(
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun failedAttachmentListWithUnifiedThemeWithoutChat() {
        snapshot(
            failedAttachmentListView(
                unifiedTheme = unifiedThemeWithoutChat()
            ).root
        )
    }

    // MARK: Secure messaging

    private fun secureMessagingView(
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null
    ) = setupView(
        chatState = ChatState()
            .changeVisibility(true)
            .setSecureMessagingState(),
        message = mediumLengthTexts()[2],
        unifiedTheme = unifiedTheme,
        uiTheme = uiTheme
    )

    @Test
    fun secureMessaging() {
        snapshot(
            secureMessagingView().root
        )
    }

    @Test
    fun secureMessagingWithUiTheme() {
        snapshot(
            secureMessagingView(
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun secureMessagingWithGlobalColors() {
        snapshot(
            secureMessagingView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun secureMessagingWithUnifiedTheme() {
        snapshot(
            secureMessagingView(
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun secureMessagingWithUnifiedThemeWithoutChat() {
        snapshot(
            secureMessagingView(
                unifiedTheme = unifiedThemeWithoutChat()
            ).root
        )
    }

    // MARK: Transferring

    private fun transferringView(
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null
    ) = setupView(
        chatState = ChatState()
            .changeVisibility(true)
            .transferring(),
        chatItems = listOf(OperatorStatusItem.Transferring),
        unifiedTheme = unifiedTheme,
        uiTheme = uiTheme
    )

    @Test
    fun transferring() {
        snapshot(
            transferringView().root
        )
    }

    @Test
    fun transferringWithUiTheme() {
        snapshot(
            transferringView(
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun transferringWithGlobalColors() {
        snapshot(
            transferringView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun transferringWithUnifiedTheme() {
        snapshot(
            transferringView(
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun transferringWithUnifiedThemeWithoutChat() {
        snapshot(
            transferringView(
                unifiedTheme = unifiedThemeWithoutChat()
            ).root
        )
    }
}
