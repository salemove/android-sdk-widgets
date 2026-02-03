package com.glia.widgets.messagecenter

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.glia.widgets.R
import com.glia.widgets.databinding.MessageCenterWelcomeViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay

/**
 * Custom view for MessageCenter welcome screen that wraps AppBarView and MessageView
 * with proper Material theme context.
 *
 * This view ensures AppBarView can resolve all required theme attributes by wrapping
 * the context with MaterialThemeOverlay before inflation.
 */
internal class MessageCenterWelcomeView(
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

    private val unifiedTheme: UnifiedTheme? by lazy { Dependencies.gliaThemeManager.theme }

    var onCloseClickListener: OnClickListener? = null

    private var binding: MessageCenterWelcomeViewBinding? = null

    val appBar: AppBarView? get() = binding?.appBarView
    val messageView: MessageView? get() = binding?.messageView

    init {
        isSaveEnabled = true
        orientation = VERTICAL
        setupViewAppearance()
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    private fun setupViewAppearance() {
        binding = MessageCenterWelcomeViewBinding.inflate(layoutInflater, this)

        appBar?.apply {
            hideBackButton()
            setTitle(LocaleString(R.string.engagement_secure_messaging_title))
            setOnXClickedListener {
                onCloseClickListener?.onClick(this@MessageCenterWelcomeView)
            }
            applyHeaderTheme(unifiedTheme?.secureMessagingWelcomeScreenTheme?.headerTheme)
        }

        SimpleWindowInsetsAndAnimationHandler(this, appBar)
    }
}