package com.glia.widgets.chat.adapter.holder

import android.view.View
import com.glia.widgets.SnapshotTest
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.ChatDeliveredItemLayoutBinding
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.junit.Before
import org.junit.Test

class DeliveredItemViewHolderSnapshotTest : SnapshotTest(), SnapshotProviders {

    @Before
    override fun setUp() {
        localeProviderMock()
    }

    @Test
    fun withDefaultTheme() {
        snapshot(
            setupView()
        )
    }

    @Test
    fun withUiTheme() {
        snapshot(
            setupView(
                uiTheme = uiTheme()
            )
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
                uiTheme = uiTheme(),
                unifiedTheme = unifiedTheme()
            )
        )
    }

    private fun setupView(
        uiTheme: UiTheme = UiTheme(),
        unifiedTheme: UnifiedTheme? = null
    ): View = DeliveredItemViewHolder(
        ChatDeliveredItemLayoutBinding.inflate(layoutInflater),
        uiTheme,
        unifiedTheme
    ).itemView

}
