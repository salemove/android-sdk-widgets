package com.glia.widgets.chat.adapter.holder

import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.core.view.MarginLayoutParamsCompat
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import kotlin.math.roundToInt

internal class SystemMessageViewHolder(
    binding: ChatReceiveMessageContentBinding,
    uiTheme: UiTheme
) : RecyclerView.ViewHolder(binding.root) {
    private val content: TextView by lazy { binding.root }
    private val stringProvider = Dependencies.getStringProvider()
    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.operatorMessage
    }

    init {
        setupTheme(uiTheme)
        updateMargins()
    }

    private fun updateMargins() {
        val lp = content.layoutParams as? MarginLayoutParams ?: return

        itemView.resources.apply {
            val marginEnd =
                getDimension(R.dimen.glia_chat_operator_margin_start) + getDimension(R.dimen.glia_chat_operator_message_end)

            MarginLayoutParamsCompat.setMarginEnd(lp, marginEnd.roundToInt())
            MarginLayoutParamsCompat.setMarginStart(lp, getDimensionPixelSize(R.dimen.glia_small))
        }
    }

    private fun setupTheme(uiTheme: UiTheme) {
        content.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            // Unified Ui
            applyLayerTheme(operatorTheme?.background)
            applyTextTheme(operatorTheme?.text)
        }
    }

    fun bind(message: String) {
        content.apply {
            text = message
            stringProvider.getRemoteString(
                R.string.android_chat_operator_message_accessibility,
                StringKeyPair(StringKey.MESSAGE, message)
            )
        }
    }
}
