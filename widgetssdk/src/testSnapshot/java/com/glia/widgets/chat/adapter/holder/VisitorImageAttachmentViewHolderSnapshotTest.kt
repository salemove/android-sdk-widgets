package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.holder.imageattachment.VisitorImageAttachmentViewHolder
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.core.fileupload.model.LocalAttachment
import com.glia.widgets.databinding.ChatAttachmentVisitorImageLayoutBinding
import com.glia.widgets.snapshotutils.SnapshotAttachment
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotGetImageFile
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotSchedulers
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class VisitorImageAttachmentViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotAttachment,
    SnapshotGetImageFile, SnapshotSchedulers, SnapshotProviders {

    // MARK: without labels

    @Test
    fun withoutLabels() {
        snapshot(
            setupView(
                visitorAttachmentItemImage()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithGlobalColors() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedTheme() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: with delivered label

    @Test
    fun deliveredLabel() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showDelivered = true)
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithGlobalColors() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showDelivered = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showDelivered = true),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showDelivered = true),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: with error label

    @Test
    fun errorLabel() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showError = true)
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithGlobalColors() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showError = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showError = true),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                visitorAttachmentItemImage(showError = true),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(val viewHolder: VisitorImageAttachmentViewHolder)

    private fun setupView(
        item: VisitorAttachmentItem.RemoteImage,
        unifiedTheme: UnifiedTheme? = null
    ): ViewData {
        val imageFileMock = getImageFileMock(R.drawable.test_banner)
        val schedulersMock = schedulersMock()

        val binding = ChatAttachmentVisitorImageLayoutBinding.inflate(layoutInflater)

        val viewHolder = VisitorImageAttachmentViewHolder(
            binding,
            imageFileMock.getImageFileFromCacheUseCaseMock,
            imageFileMock.getImageFileFromDownloadsUseCaseMock,
            imageFileMock.getImageFileFromNetworkUseCaseMock,
            schedulersMock.schedulers,
            UiTheme(),
            { },
            object : ChatAdapter.OnImageItemClickListener {
                override fun onImageItemClick(item: AttachmentFile, view: View) {}
                override fun onLocalImageItemClick(attachment: LocalAttachment, view: View) {}
            },
            unifiedTheme,
            localeProviderMock(),
        )

        viewHolder.bind(item)

        schedulersMock.triggerActions()

        return ViewData(viewHolder)
    }
}
