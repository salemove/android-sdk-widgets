package com.glia.widgets.entrywidget

import android.view.View
import androidx.core.view.allViews
import com.glia.widgets.databinding.EntryWidgetFragmentBinding
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class EntryWidgetBottomSheetTest : EntryWidgetEmbeddedViewTest() {
    override fun setupView(
        items: List<EntryWidgetContract.ItemType>,
        viewType: EntryWidgetContract.ViewType,
        unifiedTheme: UnifiedTheme?
    ): View {
        localeProviderMock()

        val entryWidgetFragment = EntryWidgetFragment()
        val binding = EntryWidgetFragmentBinding.inflate(layoutInflater)

        entryWidgetFragment.setupView(
            context,
            binding,
            unifiedTheme
        )

        binding.container.allViews.forEach {
            val entryWidgetView = it as? EntryWidgetView
            entryWidgetView?.showItems(items)
        }

        return binding.root
    }
}
