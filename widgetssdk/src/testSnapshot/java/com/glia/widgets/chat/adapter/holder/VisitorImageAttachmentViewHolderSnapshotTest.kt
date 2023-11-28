package com.glia.widgets.chat.adapter.holder

import android.graphics.BitmapFactory
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.snapshotutils.SnapshotStringProvider
import com.glia.widgets.chat.adapter.holder.imageattachment.VisitorImageAttachmentViewHolder
import com.glia.widgets.chat.model.Attachment
import com.glia.widgets.chat.model.VisitorAttachmentItem
import com.glia.widgets.databinding.ChatAttachmentVisitorImageLayoutBinding
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.snapshotutils.SnapshotChatScreen
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import io.reactivex.Maybe
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VisitorImageAttachmentViewHolderSnapshotTest : SnapshotTest(), SnapshotChatScreen {

    private fun remoteAttachment() = Attachment.Remote(
        object : AttachmentFile {
            override fun getId(): String = "imageId"

            override fun getSize(): Long = 12345

            override fun getContentType(): String = "image"

            override fun isDeleted(): Boolean = false

            override fun getName(): String ="tricky_plan.jpg"
        }
    )

    private fun image(
        attachment: Attachment = remoteAttachment(),
        showDelivered: Boolean = false,
        showError: Boolean = false
    ) = VisitorAttachmentItem.Image(
        id = "id",
        attachment = attachment,
        showDelivered = showDelivered,
        showError = showError
    )

    // MARK: without labels

    @Test
    fun withoutLabels() {
        snapshot(
            setupView(
                image()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUiTheme() {
        snapshot(
            setupView(
                image(),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithGlobalColors() {
        snapshot(
            setupView(
                image(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedTheme() {
        snapshot(
            setupView(
                image(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun withoutLabelsWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                image(),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: with delivered label

    @Test
    fun deliveredLabel() {
        snapshot(
            setupView(
                image(showDelivered = true)
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUiTheme() {
        snapshot(
            setupView(
                image(showDelivered = true),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithGlobalColors() {
        snapshot(
            setupView(
                image(showDelivered = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                image(showDelivered = true),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun deliveredLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                image(showDelivered = true),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: with error label

    @Test
    fun errorLabel() {
        snapshot(
            setupView(
                image(showError = true)
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUiTheme() {
        snapshot(
            setupView(
                image(showError = true),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithGlobalColors() {
        snapshot(
            setupView(
                image(showError = true),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedTheme() {
        snapshot(
            setupView(
                image(showError = true),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun errorLabelWithUnifiedThemeWithoutVisitorMessage() {
        snapshot(
            setupView(
                image(showError = true),
                unifiedTheme = unifiedThemeWithoutVisitorMessage()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(val viewHolder: VisitorImageAttachmentViewHolder)

    private fun setupView(
        item: VisitorAttachmentItem.Image,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme()
    ): ViewData {
        val binding = ChatAttachmentVisitorImageLayoutBinding.inflate(layoutInflater)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test_banner)

        val getImageFileFromCacheUseCase = mock<GetImageFileFromCacheUseCase>()
        whenever(getImageFileFromCacheUseCase.invoke(any())) doReturn Maybe.just(bitmap)

        val getImageFileFromDownloadsUseCase = mock<GetImageFileFromDownloadsUseCase>()
        whenever(getImageFileFromDownloadsUseCase.invoke(any())) doReturn Maybe.just(bitmap)

        val getImageFileFromNetworkUseCase = mock<GetImageFileFromNetworkUseCase>()
        whenever(getImageFileFromNetworkUseCase.invoke(any())) doReturn Maybe.just(bitmap)

        val testScheduler = TestScheduler()
        val schedulers = mock<Schedulers>()
        whenever(schedulers.computationScheduler) doReturn testScheduler
        whenever(schedulers.mainScheduler) doReturn testScheduler

        val viewHolder = VisitorImageAttachmentViewHolder(
            binding, getImageFileFromCacheUseCase, getImageFileFromDownloadsUseCase,
            getImageFileFromNetworkUseCase, schedulers, uiTheme, unifiedTheme, SnapshotStringProvider(context)
        )

        viewHolder.bind(item, { _, _ -> }) {}

        testScheduler.triggerActions()

        return ViewData(viewHolder)
    }
}
