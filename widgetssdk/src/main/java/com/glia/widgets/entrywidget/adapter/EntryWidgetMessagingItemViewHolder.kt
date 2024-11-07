package com.glia.widgets.entrywidget.adapter

import android.view.View
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.core.secureconversations.domain.ObserveUnreadMessagesCountUseCase
import com.glia.widgets.databinding.EntryWidgetMessagingItemBinding
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.entrywidget.MediaTypeItemTheme
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Locale

internal class EntryWidgetMessagingItemViewHolder(
    private val binding: EntryWidgetMessagingItemBinding,
    itemTheme: MediaTypeItemTheme?,
    private val observeUnreadMessagesCountUseCase: ObserveUnreadMessagesCountUseCase
) : EntryWidgetAdapter.ViewHolder(binding.root) {

    private var disposable: Disposable? = null

    init {
        itemTheme?.let {
            binding.root.applyLayerTheme(it.background)
            binding.title.applyTextTheme(it.title)
            binding.description.applyTextTheme(it.message)
            binding.icon.applyImageColorTheme(it.iconColor)
            it.loadingTintColor?.primaryColorStateList?.let { tintList ->
                binding.iconLoading.backgroundTintList = tintList
                binding.titleLoading.backgroundTintList = tintList
                binding.descriptionLoading.backgroundTintList = tintList
            }
            it.badge?.also(binding.unreadMessagesBadge::applyBadgeTheme)
        }
    }

    override fun bind(itemType: EntryWidgetContract.ItemType, onClickListener: View.OnClickListener) {
        binding.root.setOnClickListener(onClickListener)
        binding.root.contentDescription = null
        binding.icon.setImageResource(R.drawable.ic_secure_message)
        binding.title.setLocaleText(R.string.entry_widget_secure_messaging_button_label)
        binding.description.setLocaleText(R.string.entry_widget_secure_messaging_button_description)
        binding.description.setLocaleHint(R.string.entry_widget_secure_messaging_button_accessibility_hint)
        binding.unreadMessagesBadge.visibility = View.GONE
        binding.loadingGroup.isVisible = itemType == EntryWidgetContract.ItemType.LOADING_STATE

        disposable = observeUnreadMessagesCountUseCase()
            .subscribe { count -> updateUnreadMessageCount(count) }
    }

    private fun updateUnreadMessageCount(count: Int) {
        if (count > 0) {
            binding.unreadMessagesBadge.text = String.format(Locale.getDefault(), "%d", count)
            binding.unreadMessagesBadge.visibility = View.VISIBLE
        } else {
            binding.unreadMessagesBadge.text = ""
            binding.unreadMessagesBadge.visibility = View.GONE
        }
    }

    fun onStopView() {
        if (disposable != null) disposable?.dispose()
    }
}
