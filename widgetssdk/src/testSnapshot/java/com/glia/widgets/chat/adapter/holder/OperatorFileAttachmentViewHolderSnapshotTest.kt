package com.glia.widgets.chat.adapter.holder

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.holder.fileattachment.OperatorFileAttachmentViewHolder
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.databinding.ChatAttachmentOperatorFileLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotAttachment
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotCoil
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class OperatorFileAttachmentViewHolderSnapshotTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
), SnapshotChatScreen, SnapshotAttachment, SnapshotProviders, SnapshotCoil {

    // MARK: without header

    @Test
    fun withoutHeader() {
        snapshot(
            setupView(
                operatorAttachmentItemFile()
            ).itemView
        )
    }

    @Test
    fun withoutHeaderWithGlobalColors() {
        snapshot(
            setupView(
                operatorAttachmentItemFile(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun withoutHeaderWithUnifiedTheme() {
        snapshot(
            setupView(
                operatorAttachmentItemFile(),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun withoutHeaderWithUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                operatorAttachmentItemFile(),
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: with header

    @Test
    fun withHeader() {
        snapshot(
            setupView(
                operatorAttachmentItemFile(showChatHead = true)
            ).itemView
        )
    }

    @Test
    fun withHeaderWithGlobalColors() {
        snapshot(
            setupView(
                operatorAttachmentItemFile(showChatHead = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun withHeaderWithUnifiedTheme() {
        snapshot(
            setupView(
                operatorAttachmentItemFile(showChatHead = true),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun withHeaderWithUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                operatorAttachmentItemFile(showChatHead = true),
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: with operator header image

    private fun operatorImageHeaderItem() = operatorAttachmentItemFile(
        showChatHead = true,
        operatorProfileImgUrl = "https://worldwide-operators/best-profile-image"
    )

    @Test
    fun operatorImageHeader() {
        snapshot(
            setupView(
                operatorImageHeaderItem()
            ).itemView
        )
    }

    @Test
    fun operatorImageHeaderWithGlobalColors() {
        snapshot(
            setupView(
                operatorImageHeaderItem(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun operatorImageHeaderWithUnifiedTheme() {
        snapshot(
            setupView(
                operatorImageHeaderItem(),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun operatorImageHeaderWithUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                operatorImageHeaderItem(),
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: utils for tests

    private fun setupView(item: OperatorAttachmentItem.File, unifiedTheme: UnifiedTheme? = null): OperatorFileAttachmentViewHolder {
        mockCoil(listOf(R.drawable.test_launcher2))
        unifiedTheme?.let { Dependencies.gliaThemeManager.theme = it }

        setOnEndListener {
            Dependencies.gliaThemeManager.theme = null
        }

        return OperatorFileAttachmentViewHolder(
            ChatAttachmentOperatorFileLayoutBinding.inflate(layoutInflater),
            UiTheme(),
            object : ChatAdapter.OnFileItemClickListener {
                override fun onFileOpenClick(file: AttachmentFile) {}
                override fun onFileDownloadClick(file: AttachmentFile) {}
                override fun onLocalFileOpenClick(attachment: LocalAttachment) {}
            }
        ).also { viewHolder ->
            viewHolder.bind(item)
        }
    }
}
