package com.glia.widgets.chat.adapter.holder

import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.model.VisitorMessageItem
import com.glia.widgets.databinding.ChatVisitorMessageLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class VisitorMessageViewHolder(
    private val binding: ChatVisitorMessageLayoutBinding,
    private val onMessageClickListener: ChatAdapter.OnMessageClickListener,
    uiTheme: UiTheme,
    unifiedTheme: UnifiedTheme? = Dependencies.getGliaThemeManager().theme,
    private val stringProvider: StringProvider = Dependencies.getStringProvider()
) : RecyclerView.ViewHolder(binding.root) {

    private val visitorTheme: MessageBalloonTheme? by lazy {
        unifiedTheme?.chatTheme?.visitorMessage
    }

    private lateinit var id: String
    private var showDelivered: Boolean = false
    private var showError: Boolean = false

    init {
        uiTheme.visitorMessageBackgroundColor?.let(itemView::getColorStateListCompat)
            ?.also(binding.content::setBackgroundTintList)

        uiTheme.visitorMessageTextColor?.let(itemView::getColorCompat)
            ?.also(binding.content::setTextColor)

        if (uiTheme.fontRes != null) {
            val fontFamily = itemView.getFontCompat(uiTheme.fontRes)
            binding.content.typeface = fontFamily
            binding.deliveredView.typeface = fontFamily
            binding.errorView.typeface = fontFamily
        }
        uiTheme.baseNormalColor?.let(itemView::getColorCompat)
            ?.also(binding.deliveredView::setTextColor)

        uiTheme.systemNegativeColor?.let(itemView::getColorCompat)
            ?.also(binding.errorView::setTextColor)

        // Unified Ui
        binding.content.applyLayerTheme(visitorTheme?.background)
        binding.content.applyTextTheme(visitorTheme?.text)
        binding.deliveredView.applyTextTheme(visitorTheme?.status)
        binding.errorView.applyTextTheme(visitorTheme?.error)
        binding.deliveredView.text = stringProvider.getRemoteString(R.string.chat_message_delivered)
        binding.errorView.text = stringProvider.getRemoteString(R.string.chat_message_failed_to_deliver_retry)
    }

    fun bind(item: VisitorMessageItem) {
        this.id = item.id
        this.showDelivered = item.showDelivered
        this.showError = item.showError

        binding.content.text = item.message

        setShowError()
        setShowDelivered()
        setAccessibilityLabels()
    }

    private fun setShowDelivered() {
        binding.deliveredView.isVisible = !showError && showDelivered
    }

    private fun setShowError() {
        binding.errorView.isVisible = showError

        if (showError) {
            itemView.setOnClickListener { onMessageClickListener.onMessageClick(id) }
            binding.content.setOnClickListener { onMessageClickListener.onMessageClick(id) }
        } else {
            itemView.setOnClickListener(null)
            binding.content.setOnClickListener(null)
        }
    }

    private fun setAccessibilityLabels() {
        itemView.contentDescription = stringProvider.getRemoteString(
            if (showError) {
                R.string.android_chat_visitor_message_not_delivered_accessibility
            } else if (showDelivered) {
                R.string.android_chat_visitor_message_delivered_accessibility
            } else {
                R.string.android_chat_visitor_message_accessibility
            },
            StringKeyPair(StringKey.MESSAGE, binding.content.text.toString())
        )

        ViewCompat.setAccessibilityDelegate(itemView, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                if (showError) {
                    val actionLabel = stringProvider.getRemoteString(R.string.general_retry)
                    val actionClick = AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel
                    )
                    info.addAction(actionClick)
                } else {
                    info.removeAction(AccessibilityActionCompat.ACTION_CLICK)
                    info.isClickable = false
                }
            }
        })
    }

    fun updateDelivered(delivered: Boolean) {
        this.showDelivered = delivered
        setShowDelivered()
        setAccessibilityLabels()
    }

    fun updateError(showError: Boolean) {
        this.showError = showError
        setShowError()
        setAccessibilityLabels()
    }
}
