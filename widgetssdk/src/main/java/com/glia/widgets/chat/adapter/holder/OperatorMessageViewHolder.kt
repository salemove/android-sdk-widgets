package com.glia.widgets.chat.adapter.holder

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.model.OperatorMessageItem
import com.glia.widgets.databinding.ChatOperatorMessageLayoutBinding
import com.glia.widgets.databinding.ChatReceiveMessageContentBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.view.SingleChoiceCardView
import com.glia.widgets.view.SingleChoiceCardView.OnOptionClickedListener
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.MessageBalloonTheme

internal class OperatorMessageViewHolder(
    private val binding: ChatOperatorMessageLayoutBinding,
    private val uiTheme: UiTheme
) : RecyclerView.ViewHolder(binding.root) {
    private val operatorTheme: MessageBalloonTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.operatorMessage
    }
    private val stringProvider = Dependencies.getStringProvider()
    private val messageContentView: TextView by lazy {
        ChatReceiveMessageContentBinding.inflate(
            itemView.layoutInflater,
            binding.contentLayout,
            false
        ).root.apply {
            uiTheme.operatorMessageBackgroundColor?.let(::getColorStateListCompat)?.also {
                backgroundTintList = it
            }
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setTextColor)
            uiTheme.operatorMessageTextColor?.let(::getColorCompat)?.also(::setLinkTextColor)

            uiTheme.fontRes?.let(::getFontCompat)?.also(::setTypeface)

            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO

            // Unified Ui
            applyLayerTheme(operatorTheme?.background)
            applyTextTheme(operatorTheme?.text)
        }
    }

    init {
        setupOperatorStatusView()
    }

    private fun setupOperatorStatusView() {
        binding.chatHeadView.setTheme(uiTheme)
        binding.chatHeadView.setShowRippleAnimation(false)
        binding.chatHeadView.applyUserImageTheme(operatorTheme?.userImage)
    }

    fun bind(
        item: OperatorMessageItem,
        onOptionClickedListener: OnOptionClickedListener
    ) {
        binding.contentLayout.removeAllViews()
        when (item) {
            is OperatorMessageItem.PlainText -> addMessageTextView(item)
            is OperatorMessageItem.ResponseCard -> addSingleChoiceCardView(item, onOptionClickedListener)
        }
        updateOperatorStatusView(item)
    }

    private fun addSingleChoiceCardView(
        item: OperatorMessageItem.ResponseCard,
        onOptionClickedListener: OnOptionClickedListener
    ) {
        val singleChoiceCardView = SingleChoiceCardView(itemView.context)
        singleChoiceCardView.setOnOptionClickedListener(onOptionClickedListener)
        singleChoiceCardView.setData(
            item,
            uiTheme
        )
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(
            0,
            itemView.resources.getDimensionPixelSize(R.dimen.glia_medium),
            0,
            0
        )
        binding.contentLayout.addView(singleChoiceCardView, params)
        itemView.contentDescription = item.content
    }

    private fun addMessageTextView(item: OperatorMessageItem.PlainText) {
        messageContentView.text = item.content
        binding.contentLayout.addView(messageContentView)
        if (!TextUtils.isEmpty(item.operatorName)) {
            itemView.contentDescription = stringProvider.getRemoteString(
                R.string.android_chat_operator_name_accessibility_message,
                StringKeyPair(StringKey.OPERATOR_NAME, item.operatorName ?: "" ),
                StringKeyPair(StringKey.MESSAGE, item.content ?: "")
            )
        } else {
            itemView.contentDescription = stringProvider.getRemoteString(
                R.string.android_chat_accessibility_message,
                StringKeyPair(StringKey.MESSAGE, item.content ?: "")
            )
        }
    }

    private fun updateOperatorStatusView(item: OperatorMessageItem) {
        binding.chatHeadView.isVisible = item.showChatHead
        if (item.operatorProfileImgUrl != null) {
            binding.chatHeadView.showProfileImage(item.operatorProfileImgUrl)
        } else {
            binding.chatHeadView.showPlaceholder()
        }
    }
}
