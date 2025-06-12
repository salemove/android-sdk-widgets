package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.holder.imageattachment.OperatorImageAttachmentViewHolder
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.databinding.ChatAttachmentOperatorImageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotAttachment
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotGetImageFile
import com.glia.widgets.snapshotutils.SnapshotCoil
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotSchedulers
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class OperatorImageAttachmentViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotAttachment, SnapshotGetImageFile,
    SnapshotSchedulers, SnapshotProviders, SnapshotCoil {

    // MARK: without header

    @Test
    fun withoutHeader() {
        snapshot(
            setupView(
                operatorAttachmentItemImage()
            ).itemView
        )
    }

    @Test
    fun withoutHeaderWithGlobalColors() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun withoutHeaderWithUnifiedTheme() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun withoutHeaderWithUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(),
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: with header

    @Test
    fun withHeader() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(showChatHead = true)
            ).itemView
        )
    }

    @Test
    fun withHeaderWithGlobalColors() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(showChatHead = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).itemView
        )
    }

    @Test
    fun withHeaderWithUnifiedTheme() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(showChatHead = true),
                unifiedTheme = unifiedTheme()
            ).itemView
        )
    }

    @Test
    fun withHeaderWithUnifiedThemeWithoutOperatorMessage() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(showChatHead = true),
                unifiedTheme = unifiedThemeWithoutOperatorMessage()
            ).itemView
        )
    }

    // MARK: with operator header image

    private fun operatorImageHeaderItem() = operatorAttachmentItemImage(
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

    private fun setupView(item: OperatorAttachmentItem.Image, unifiedTheme: UnifiedTheme? = null): OperatorImageAttachmentViewHolder {
        unifiedTheme?.let { Dependencies.gliaThemeManager.theme = it }

        mockCoil(listOf(R.drawable.test_launcher2))
        val imageFileMock = getImageFileMock(R.drawable.test_banner)
        val schedulersMock = schedulersMock()

        val binding = ChatAttachmentOperatorImageLayoutBinding.inflate(layoutInflater)

        val viewHolder = OperatorImageAttachmentViewHolder(
            binding, imageFileMock.getImageFileFromCacheUseCaseMock, imageFileMock.getImageFileFromDownloadsUseCaseMock,
            imageFileMock.getImageFileFromNetworkUseCaseMock, schedulersMock.schedulers, UiTheme(), object : ChatAdapter.OnImageItemClickListener {
                override fun onImageItemClick(item: AttachmentFile, view: View) {}
                override fun onLocalImageItemClick(attachment: LocalAttachment, view: View) {}
            }
        )

        viewHolder.bind(item)

        schedulersMock.triggerActions()

        setOnEndListener {
            Dependencies.gliaThemeManager.theme = null
        }

        return viewHolder
    }
}
