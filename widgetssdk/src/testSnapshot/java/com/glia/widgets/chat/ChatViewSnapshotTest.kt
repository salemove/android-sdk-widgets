package com.glia.widgets.chat

import android.widget.ImageView
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.OperatorStatusItem
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.entrywidget.EntryWidgetView
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

    private fun attachmentListView(unifiedTheme: UnifiedTheme? = null) = setupView(
        chatState = ChatState()
            .initChat()
            .changeVisibility(true)
            .engagementStarted()
            .operatorConnected("Snap Test", null)
            .setSendButtonEnabled(true)
            .setIsAttachmentButtonEnabled(true),
        fileAttachments = listOf(
            fileAttachment(
                displayName = "test.doc",
                status = LocalAttachment.Status.READY_TO_SEND
            ),
            fileAttachment(
                displayName = "photo.jpg",
                isImage = true,
                size = 123456,
                status = LocalAttachment.Status.SECURITY_SCAN
            ),
            fileAttachment(
                displayName = "image.jpg",
                size = 654321,
                isImage = true
            ),
            fileAttachment(
                displayName = "failed.file",
                status = LocalAttachment.Status.ERROR_SECURITY_SCAN_FAILED
            )
        ),
        imageResources = listOf(
            R.drawable.test_banner,
            R.drawable.test_launcher
        ),
        message = mediumLengthTexts()[0],
        unifiedTheme = unifiedTheme
    )

    @Test
    fun attachmentList() {
        snapshot(
            attachmentListView().root
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

    private fun failedAttachmentListView(unifiedTheme: UnifiedTheme? = null) = setupView(
        chatState = ChatState()
            .initChat()
            .changeVisibility(true)
            .engagementStarted()
            .setIsAttachmentButtonEnabled(false)
            .setSendButtonEnabled(false),
        fileAttachments = listOf(
            fileAttachment(
                displayName = "test.doc",
                status = LocalAttachment.Status.ERROR_NETWORK_TIMEOUT
            ),
            fileAttachment(
                displayName = "photo.jpg",
                isImage = true,
                size = 123456,
                status = LocalAttachment.Status.ERROR_SECURITY_SCAN_FAILED
            )
        ),
        imageResources = listOf(
            R.drawable.test_banner,
            R.drawable.test_launcher
        ),
        message = mediumLengthTexts()[1],
        unifiedTheme = unifiedTheme
    )

    @Test
    fun failedAttachmentList() {
        snapshot(
            failedAttachmentListView().root
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
        isUnavailable: Boolean = false,
        showTopBanner: Boolean = false,
        clickTopBanner: Boolean = false,
    ) = setupView(
        chatState = ChatState()
            .initChat()
            .setSecureMessagingState()
            .setSecureConversationsTopBannerVisibility(showTopBanner)
            .setIsAttachmentButtonEnabled(!isUnavailable)
            .let { if (isUnavailable) it.setSecureMessagingUnavailable() else it },
        message = mediumLengthTexts()[2],
        unifiedTheme = unifiedTheme
    )
        .apply {
            if (clickTopBanner) {
                chatView.findViewById<EntryWidgetView>(R.id.sc_top_banner_options).showItems(defaultMediaTypesForTopBanner)
                chatView.findViewById<ImageView>(R.id.sc_top_banner_icon).performClick()
            }
        }

    @Test
    fun secureMessaging() {
        snapshot(
            secureMessagingView().root
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

    @Test
    fun secureMessagingUnavailable() {
        snapshot(
            secureMessagingView(isUnavailable = true).root
        )
    }

    @Test
    fun secureMessagingUnavailableWithGlobalColors() {
        snapshot(
            secureMessagingView(
                unifiedTheme = unifiedThemeWithGlobalColors(),
                isUnavailable = true
            ).root
        )
    }

    @Test
    fun secureMessagingUnavailableWithUnifiedTheme() {
        snapshot(
            secureMessagingView(
                unifiedTheme = unifiedTheme(),
                isUnavailable = true
            ).root
        )
    }

    private val defaultMediaTypesForTopBanner = listOf(
        EntryWidgetContract.ItemType.VideoCall,
        EntryWidgetContract.ItemType.AudioCall,
        EntryWidgetContract.ItemType.Chat
    )

    @Test
    fun secureMessagingWithTopBannerOpen() {
        snapshot(
            secureMessagingView(
                showTopBanner = true,
                clickTopBanner = true
            ).root
        )
    }

    @Test
    fun secureMessagingWithTopBannerOpenWithGlobalColors() {
        snapshot(
            secureMessagingView(
                unifiedTheme = unifiedThemeWithGlobalColors(),
                showTopBanner = true,
                clickTopBanner = true
            ).root
        )
    }

    @Test
    fun secureMessagingWithTopBannerClosed() {
        snapshot(
            secureMessagingView(
                showTopBanner = true
            ).root
        )
    }

    @Test
    fun secureMessagingWithTopBannerClosedWithGlobalColors() {
        snapshot(
            secureMessagingView(
                unifiedTheme = unifiedThemeWithGlobalColors(),
                showTopBanner = true
            ).root
        )
    }

    // MARK: Transferring

    private fun transferringView(unifiedTheme: UnifiedTheme? = null) = setupView(
        chatState = ChatState()
            .changeVisibility(true)
            .transferring(),
        chatItems = listOf(OperatorStatusItem.Transferring),
        unifiedTheme = unifiedTheme
    )

    @Test
    fun transferring() {
        snapshot(
            transferringView().root
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
