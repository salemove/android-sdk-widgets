package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.holder.imageattachment.VisitorImageAttachmentViewHolder
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.databinding.ChatAttachmentVisitorImageLayoutBinding
import com.glia.widgets.snapshotutils.SnapshotAttachment
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.snapshotutils.SnapshotGetImageFile
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotSchedulers
import org.junit.Test

internal class VisitorImageAttachmentViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen, SnapshotAttachment,
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
    // MARK: utils for tests

    private data class ViewData(val viewHolder: VisitorImageAttachmentViewHolder)

    private fun setupView(item: VisitorAttachmentItem.RemoteImage): ViewData {
        val imageFileMock = getImageFileMock(R.drawable.test_banner)
        val schedulersMock = schedulersMock()

        val binding = ChatAttachmentVisitorImageLayoutBinding.inflate(layoutInflater)

        val viewHolder = VisitorImageAttachmentViewHolder(
            binding,
            imageFileMock.getImageFileFromCacheUseCaseMock,
            imageFileMock.getImageFileFromDownloadsUseCaseMock,
            imageFileMock.getImageFileFromNetworkUseCaseMock,
            schedulersMock.schedulers,
            { },
            object : ChatAdapter.OnImageItemClickListener {
                override fun onImageItemClick(item: AttachmentFile, view: View) {}
                override fun onLocalImageItemClick(attachment: LocalAttachment, view: View) {}
            },
            localeProviderMock(),
        )

        viewHolder.bind(item)

        schedulersMock.triggerActions()

        return ViewData(viewHolder)
    }
}
