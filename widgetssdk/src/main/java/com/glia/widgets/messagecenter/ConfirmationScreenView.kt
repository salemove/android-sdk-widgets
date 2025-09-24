package com.glia.widgets.messagecenter

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.glia.widgets.R
import com.glia.widgets.databinding.MessageCenterConfirmationViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingConfirmationScreenTheme
import com.google.android.material.transition.MaterialFadeThrough

internal class ConfirmationScreenView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : LinearLayout(
    context.wrapWithMaterialThemeOverlay(attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
) {

    private val unifiedTheme: SecureMessagingConfirmationScreenTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.secureMessagingConfirmationScreenTheme
    }

    private val binding: MessageCenterConfirmationViewBinding by lazy {
        MessageCenterConfirmationViewBinding.inflate(layoutInflater, this)
    }

    private val checkMessagesButton: Button get() = binding.btnCheckMessagesConfirmationScreen

    private var onCheckMessagesButtonClickListener: OnClickListener? = null

    init {
        gravity = Gravity.CENTER
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(this.context, R.color.glia_chat_background_color))
        checkMessagesButton.setOnClickListener { onCheckMessagesButtonClickListener?.onClick(it) }
        binding.title.setLocaleText(R.string.general_thank_you)
        binding.subtitle.setLocaleText(R.string.message_center_confirmation_subtitle)
        binding.btnCheckMessagesConfirmationScreen.setLocaleText(R.string.message_center_welcome_check_messages)
        binding.btnCheckMessagesConfirmationScreen.setLocaleContentDescription(
            R.string.message_center_confirmation_check_messages_accessibility_hint
        )
        setupUnifiedTheme()
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle) : this(
        context,
        attrs,
        defStyleAttr,
        R.style.Application_Glia_Chat
    )

    private fun setupUnifiedTheme() {
        unifiedTheme?.apply {
            applyColorTheme(backgroundTheme)
            binding.confirmationIcon.applyImageColorTheme(iconColorTheme)
            binding.title.applyTextTheme(titleTheme)
            binding.subtitle.applyTextTheme(subtitleTheme)
            binding.btnCheckMessagesConfirmationScreen.applyButtonTheme(checkMessagesButtonTheme)
        }
    }

    fun setOnCheckMessagesButtonClickListener(listener: OnClickListener) {
        onCheckMessagesButtonClickListener = listener
    }

    fun fadeThrough(viewToHide: View) {
        TransitionManager.beginDelayedTransition(parent as ViewGroup, MaterialFadeThrough())
        viewToHide.isGone = true
        isVisible = true
    }
}
