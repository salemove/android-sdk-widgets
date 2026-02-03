package com.glia.widgets.messagecenter

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.MessageCenterConfirmationScreenViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.defaulttheme.DefaultHeader
import com.google.android.material.theme.overlay.MaterialThemeOverlay

/**
 * Custom view for MessageCenter confirmation screen that wraps AppBarView and ConfirmationScreenView
 * with proper Material theme context.
 *
 * This view ensures AppBarView can resolve all required theme attributes by wrapping
 * the context with MaterialThemeOverlay before inflation.
 */
internal class MessageCenterConfirmationView(
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

    private var binding: MessageCenterConfirmationScreenViewBinding? = null

    val appBar: AppBarView? get() = binding?.appBarView
    val confirmationView: ConfirmationScreenView? get() = binding?.confirmationView

    init {
        isSaveEnabled = true
        orientation = VERTICAL
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        setupViewAppearance()
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    private fun setupViewAppearance() {
        binding = MessageCenterConfirmationScreenViewBinding.inflate(layoutInflater, this)

        // Apply confirmation screen theme
        val uiTheme = UiTheme()
        val primaryColorId = uiTheme.brandPrimaryColor ?: R.color.glia_primary_color
        val baseLightColorId = uiTheme.baseLightColor ?: R.color.glia_light_color

        val appBarTheme = DefaultHeader(
            ColorTheme(getColorCompat(primaryColorId)),
            ColorTheme(getColorCompat(baseLightColorId)),
            null
        ) merge unifiedTheme?.secureMessagingConfirmationScreenTheme?.headerTheme

        appBar?.apply {
            hideBackButton()
            setTitle(LocaleString(R.string.engagement_secure_messaging_title))
            setOnXClickedListener {
                onCloseClickListener?.onClick(this@MessageCenterConfirmationView)
            }
            resetTheme()
            applyHeaderTheme(appBarTheme)
        }

        SimpleWindowInsetsAndAnimationHandler(this, appBar)
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        binding?.appBarView?.setTheme(Utils.getThemeFromTypedArray(typedArray, this.context))
    }
}