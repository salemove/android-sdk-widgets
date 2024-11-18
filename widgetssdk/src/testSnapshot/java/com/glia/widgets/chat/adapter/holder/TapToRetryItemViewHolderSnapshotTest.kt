package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.ChatTapToRetryItemLayoutBinding
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Test

internal class TapToRetryItemViewHolderSnapshotTest : SnapshotTest() {

    @Test
    fun withDefaultTheme() {
        snapshot(
            setupView()
        )
    }

    @Test
    fun withGlobalColors() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            )
        )
    }

    @Test
    fun withUnifiedTheme() {
        snapshot(
            setupView(
                unifiedTheme = unifiedTheme()
            )
        )
    }

    private fun setupView(
        unifiedTheme: UnifiedTheme? = null
    ): View = TapToRetryItemViewHolder(
        ChatTapToRetryItemLayoutBinding.inflate(layoutInflater),
        UiTheme(),
        {},
        unifiedTheme
    ).itemView

}
