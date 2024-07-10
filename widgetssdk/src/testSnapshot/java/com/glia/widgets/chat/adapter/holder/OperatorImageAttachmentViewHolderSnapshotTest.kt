package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.holder.imageattachment.OperatorImageAttachmentViewHolder
import com.glia.widgets.chat.model.OperatorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentOperatorImageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotAttachment
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotGetImageFile
import com.glia.widgets.snapshotutils.SnapshotPicasso
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotSchedulers
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class OperatorImageAttachmentViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotAttachment, SnapshotGetImageFile,
    SnapshotSchedulers, SnapshotProviders, SnapshotPicasso {

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
    fun withoutHeaderWithUiTheme() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(),
                uiTheme = uiTheme()
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
    fun withHeaderWithUiTheme() {
        snapshot(
            setupView(
                operatorAttachmentItemImage(showChatHead = true),
                uiTheme = uiTheme()
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
    fun operatorImageHeaderWithUiTheme() {
        snapshot(
            setupView(
                operatorImageHeaderItem(),
                uiTheme = uiTheme()
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

    private fun setupView(
        item: OperatorAttachmentItem.Image,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): OperatorImageAttachmentViewHolder {
        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        picassoMock(listOf(R.drawable.test_launcher2))
        val imageFileMock = getImageFileMock(R.drawable.test_banner)
        val schedulersMock = schedulersMock()
        localeProviderMock()

        val binding = ChatAttachmentOperatorImageLayoutBinding.inflate(layoutInflater)

        val viewHolder = OperatorImageAttachmentViewHolder(
            binding, imageFileMock.getImageFileFromCacheUseCaseMock, imageFileMock.getImageFileFromDownloadsUseCaseMock,
            imageFileMock.getImageFileFromNetworkUseCaseMock, schedulersMock.schedulers, uiTheme
        )

        viewHolder.bind(item) { _, _ -> }

        schedulersMock.triggerActions()

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
        }

        return viewHolder
    }
}
