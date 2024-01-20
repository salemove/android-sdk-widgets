package com.glia.widgets.chat.adapter.holder

import android.view.View
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.RawRes
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding
import com.glia.widgets.snapshotutils.SnapshotGva
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class GvaGalleryItemViewHolderSnapshotTest : SnapshotTest(), SnapshotGva {

    // MARK: tests with all views

    private fun allViewsCard() = GvaGalleryCard(
        title = "Title",
        subtitle = "Subtitle",
        options = listOf(
            GvaButton("Button 1"),
            GvaButton("Button 2")
        )
    )

    @Test
    fun allViews() {
        snapshot(
            setupView(
                allViewsCard(),
                R.drawable.test_banner
            ).viewHolder.itemView
        )
    }

    @Test
    fun allViewsWithUiTheme() {
        snapshot(
            setupView(
                allViewsCard(),
                R.drawable.test_banner,
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun allViewsWithGlobalColors() {
        snapshot(
            setupView(
                allViewsCard(),
                R.drawable.test_banner,
                unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun allViewsWithUnifiedTheme() {
        snapshot(
            setupView(
                allViewsCard(),
                R.drawable.test_banner,
                unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun allViewsWithUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                allViewsCard(),
                R.drawable.test_banner,
                unifiedThemeWithoutGva()
            ).viewHolder.itemView
        )
    }

    // MARK: tests with mandatory view

    private fun onlyMandatoryCard() = GvaGalleryCard(
        title = "Title"
    )

    @Test
    fun onlyMandatory() {
        snapshot(
            setupView(
                onlyMandatoryCard()
            ).viewHolder.itemView
        )
    }

    @Test
    fun onlyMandatoryWithUiTheme() {
        snapshot(
            setupView(
                onlyMandatoryCard(),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun onlyMandatoryWithGlobalColors() {
        snapshot(
            setupView(
                onlyMandatoryCard(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun onlyMandatoryWithUnifiedTheme() {
        snapshot(
            setupView(
                onlyMandatoryCard(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun onlyMandatoryWithUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                onlyMandatoryCard(),
                unifiedTheme = unifiedThemeWithoutGva()
            ).viewHolder.itemView
        )
    }

    // MARK: tests with long title

    private fun longTitleCard() = GvaGalleryCard(
        title = gvaLongTitle()
    )

    @Test
    fun longTitle() {
        snapshot(
            setupView(
                longTitleCard()
            ).viewHolder.itemView
        )
    }

    @Test
    fun longTitleWithUiTheme() {
        snapshot(
            setupView(
                longTitleCard(),
                uiTheme = uiTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun longTitleWithGlobalColors() {
        snapshot(
            setupView(
                longTitleCard(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun longTitleWithUnifiedTheme() {
        snapshot(
            setupView(
                longTitleCard(),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun longTitleWithUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                longTitleCard(),
                unifiedTheme = unifiedThemeWithoutGva()
            ).viewHolder.itemView
        )
    }

    // MARK: tests with long subtitle

    private fun longSubtitleCard() = GvaGalleryCard(
        title = "Title",
        subtitle = gvaLongSubtitle()
    )

    @Test
    fun longSubtitle() {
        snapshot(
            setupView(
                longSubtitleCard(),
                height = 700
            ).viewHolder.itemView
        )
    }

    @Test
    fun longSubtitleWithUiTheme() {
        snapshot(
            setupView(
                longSubtitleCard(),
                uiTheme = uiTheme(),
                height = 740
            ).viewHolder.itemView
        )
    }

    @Test
    fun longSubtitleWithGlobalColors() {
        snapshot(
            setupView(
                longSubtitleCard(),
                unifiedTheme = unifiedThemeWithGlobalColors(),
                height = 700
            ).viewHolder.itemView
        )
    }

    @Test
    fun longSubtitleWithUnifiedTheme() {
        snapshot(
            setupView(
                longSubtitleCard(),
                unifiedTheme = unifiedTheme(),
                height = 500
            ).viewHolder.itemView
        )
    }

    @Test
    fun longSubtitleWithUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                longSubtitleCard(),
                unifiedTheme = unifiedThemeWithoutGva(),
                height = 600
            ).viewHolder.itemView
        )
    }

    // MARK: tests with long subtitle

    private fun longButtonsTitleCard() = GvaGalleryCard(
        title = "Title",
        options = mediumLengthTexts().map { GvaButton(it) }
    )

    @Test
    fun longButtonsTitle() {
        val itemView = setupView(
            longButtonsTitleCard()
        ).viewHolder.itemView
        measureHeight(itemView)

        snapshot(itemView)
    }

    @Test
    fun longButtonsTitleWithUiTheme() {
        val itemView = setupView(
            longButtonsTitleCard(),
            uiTheme = uiTheme()

        ).viewHolder.itemView
        measureHeight(itemView)

        snapshot(itemView)
    }

    @Test
    fun longButtonsTitleWithGlobalColors() {
        val itemView = setupView(
            longButtonsTitleCard(),
            unifiedTheme = unifiedThemeWithGlobalColors()

        ).viewHolder.itemView
        measureHeight(itemView)

        snapshot(itemView)
    }

    @Test
    fun longButtonsTitleWithUnifiedTheme() {
        val itemView = setupView(
            longButtonsTitleCard(),
            unifiedTheme = unifiedTheme()

        ).viewHolder.itemView
        measureHeight(itemView)

        snapshot(itemView)
    }

    @Test
    fun longButtonsTitleWithUnifiedThemeWithoutGva() {
        val itemView = setupView(
            longButtonsTitleCard(),
            unifiedTheme = unifiedThemeWithoutGva()

        ).viewHolder.itemView
        measureHeight(itemView)

        snapshot(itemView)
    }

    // MARK: utils for tests

    private data class ViewData(val binding: ChatGvaGalleryItemBinding, val viewHolder: GvaGalleryItemViewHolder)

    private fun setupView(
        card: GvaGalleryCard,
        @RawRes imageRes: Int? = null,
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme = UiTheme(),
        height: Int? = null
    ): ViewData {
        val binding = ChatGvaGalleryItemBinding.inflate(layoutInflater)
        val viewHolder = GvaGalleryItemViewHolder(binding, {}, uiTheme, unifiedTheme)

        viewHolder.bind(card, 1, 4)

        imageRes?.also {
            // Set image without Picasso
            binding.image.setImageResource(it)
            binding.image.visibility = View.VISIBLE
        }

        height?.also {
            viewHolder.itemView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, it)
        }

        return ViewData(binding, viewHolder)
    }

    private fun measureHeight(view: View) {
        val width = resources.getDimensionPixelOffset(R.dimen.glia_chat_gva_gallery_card_width)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
    }
}
