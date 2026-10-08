package com.glia.widgets.chat.adapter.holder

import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.core.view.MarginLayoutParamsCompat
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.gliaAttrFont
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme
import kotlin.math.roundToInt

internal class SystemMessageViewHolder(
    binding: ChatReceiveMessageContentBinding
) : RecyclerView.ViewHolder(binding.root) {
    private val content: TextView by lazy { binding.root }
    private val localeProvider = Dependencies.localeProvider
    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.chatTheme?.operatorMessage
    }

    init {
        setupTheme()
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

    /** The bubble's colours come from the layout's `?attr/gliaOperatorMessage*`. */
    private fun setupTheme() {
        content.apply {
            context.gliaAttrFont()?.also(::setTypeface)

            // Unified Ui
            applyLayerTheme(operatorTheme?.background)
            applyTextTheme(operatorTheme?.text)
        }
    }

    fun bind(message: String) {
        content.apply {
            text = message
            localeProvider.getString(
                R.string.android_chat_operator_message_accessibility,
                StringKeyPair(StringKey.MESSAGE, message)
            )
        }
    }
}
