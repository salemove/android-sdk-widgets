package com.glia.widgets.entrywidget

import android.view.View
import androidx.core.view.allViews
import com.glia.widgets.core.secureconversations.domain.ObserveUnreadMessagesCountUseCase
import com.glia.widgets.databinding.EntryWidgetFragmentBinding
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

internal class EntryWidgetBottomSheetTest : EntryWidgetEmbeddedViewTest() {
    override fun setupView(
        items: List<EntryWidgetContract.ItemType>,
        viewType: EntryWidgetContract.ViewType,
        observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase,
        unifiedTheme: UnifiedTheme?
    ): View {
        localeProviderMock()

        val entryWidgetFragment = EntryWidgetFragment()
        val binding = EntryWidgetFragmentBinding.inflate(layoutInflater)

        entryWidgetFragment.setupView(
            context,
            binding,
            unifiedTheme?.entryWidgetTheme,
            observeUnreadMessagesCountUseCase,
        )

        binding.container.allViews.forEach {
            val entryWidgetView = it as? EntryWidgetView
            entryWidgetView?.showItems(items)
        }

        return binding.root
    }
}
