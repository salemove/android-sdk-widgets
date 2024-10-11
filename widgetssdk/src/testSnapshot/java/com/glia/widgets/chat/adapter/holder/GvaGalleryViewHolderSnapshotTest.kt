package com.glia.widgets.chat.adapter.holder

import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatItemHeightManager
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.chat.model.GvaGalleryCards
import com.glia.widgets.databinding.ChatGvaGalleryLayoutBinding
import com.glia.widgets.snapshotutils.SnapshotGva
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

class GvaGalleryViewHolderSnapshotTest : SnapshotTest(), SnapshotGva, SnapshotProviders {

    private fun galleryCardList() = listOf(
        GvaGalleryCard(
            title = "Gallery card title",
            subtitle = gvaLongSubtitle(),
            options = listOf(
                GvaButton("Button 1"),
                GvaButton("Button 2")
            )
        ),
        GvaGalleryCard(
            title = "Buttons gallery card",
            options = mediumLengthTexts().map { GvaButton(it) }
        )
    )

    // MARK: tests for view without chat head

    @Test
    fun itemsWithoutChatHead() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList()
                )
            ).viewHolder.itemView
        )
    }

    @Test
    fun itemsWithoutChatHeadWithGlobalColors() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList()
                ),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun itemsWithoutChatHeadWithUnifiedTheme() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList()
                ),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun itemsWithoutChatHeadWithUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList()
                ),
                unifiedTheme = unifiedThemeWithoutGva()
            ).viewHolder.itemView
        )
    }

    // MARK: tests for view with chat head

    @Test
    fun itemsWithChatHead() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList(),
                    showChatHead = true
                )
            ).viewHolder.itemView
        )
    }

    @Test
    fun itemsWithChatHeadWithGlobalColors() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList(),
                    showChatHead = true
                ),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).viewHolder.itemView
        )
    }

    @Test
    fun itemsWithChatHeadWithUnifiedTheme() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList(),
                    showChatHead = true
                ),
                unifiedTheme = unifiedTheme()
            ).viewHolder.itemView
        )
    }

    @Test
    fun itemsWithChatHeadWithUnifiedThemeWithoutGva() {
        snapshot(
            setupView(
                GvaGalleryCards(
                    galleryCards = galleryCardList(),
                    showChatHead = true
                ),
                unifiedTheme = unifiedThemeWithoutGva()
            ).viewHolder.itemView
        )
    }

    // MARK: utils for tests

    private data class ViewData(val binding: ChatGvaGalleryLayoutBinding, val viewHolder: GvaGalleryViewHolder)

    private fun setupView(galleryCards: GvaGalleryCards, unifiedTheme: UnifiedTheme? = null): ViewData {
        localeProviderMock()

        val heightManager = ChatItemHeightManager(UiTheme(), layoutInflater, resources, unifiedTheme)
        heightManager.measureHeight(listOf(galleryCards))

        val binding = ChatGvaGalleryLayoutBinding.inflate(layoutInflater)
        val viewHolder = GvaGalleryViewHolder(binding, {}, UiTheme(), unifiedTheme)

        viewHolder.bind(galleryCards, heightManager.getMeasuredHeight(galleryCards))

        return ViewData(binding, viewHolder)
    }
}
