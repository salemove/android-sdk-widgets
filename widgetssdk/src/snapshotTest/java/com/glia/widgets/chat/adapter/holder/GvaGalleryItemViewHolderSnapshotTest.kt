package com.glia.widgets.chat.adapter.holder

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.GvaButton
import com.glia.widgets.chat.model.GvaGalleryCard
import com.glia.widgets.databinding.ChatGvaGalleryItemBinding
import org.junit.Rule
import org.junit.Test

class GvaGalleryItemViewHolderSnapshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_4A,
        renderingMode = SessionParams.RenderingMode.SHRINK,
        showSystemUi = false,
        theme = "ThemeOverlay_Glia_Chat_Material"
    )

    @Test
    fun testHolder() {
        val binding = ChatGvaGalleryItemBinding.inflate(paparazzi.layoutInflater)
        val viewHolder = GvaGalleryItemViewHolder(binding, {}, UiTheme())

        val gvaGalleryCard = GvaGalleryCard(
            title = "Title",
            subtitle = "Subtitle",
            options = listOf(
                GvaButton("Button 1"),
                GvaButton("Button 2")
            )
        )
        viewHolder.bind(gvaGalleryCard, 1, 4)

        paparazzi.snapshot(binding.root)
    }
}
