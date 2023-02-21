package com.glia.widgets.messagecenter

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.glia.widgets.R
import com.glia.widgets.databinding.MessageCenterConfirmationViewBinding
import com.glia.widgets.view.unifiedui.extensions.layoutInflater
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.google.android.material.transition.MaterialFadeThrough

class ConfirmationScreenView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : LinearLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
) {

    private val binding: MessageCenterConfirmationViewBinding by lazy {
        MessageCenterConfirmationViewBinding.inflate(layoutInflater, this)
    }

    private val checkMessagesButton: Button get() = binding.btnCheckMessagesConfirmationScreen

    private var onCheckMessagesButtonClickListener: OnClickListener? = null

    init {
        gravity = Gravity.CENTER
        orientation = VERTICAL
        checkMessagesButton.setOnClickListener { onCheckMessagesButtonClickListener?.onClick(it) }

    }

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    fun setOnCheckMessagesButtonClickListener(listener: OnClickListener) {
        onCheckMessagesButtonClickListener = listener
    }

    fun fadeThrough(viewToHide: View) {
        TransitionManager.beginDelayedTransition(parent as ViewGroup, MaterialFadeThrough())
        viewToHide.isGone = true
        isVisible = true
    }

}