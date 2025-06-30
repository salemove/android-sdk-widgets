package com.glia.widgets.view.header

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.AppBarBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.applyButtonTheme
import com.glia.widgets.helper.applyIconColorTheme
import com.glia.widgets.helper.applyTextTheme
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.getTypedArrayResId
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleNavigationContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.helper.setText
import com.glia.widgets.helper.setTintCompat
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.android.material.appbar.AppBarLayout

internal class AppBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.gliaChatStyle
) : AppBarLayout(context, attrs, defStyleAttr) {
    private val binding: AppBarBinding by lazy { AppBarBinding.inflate(layoutInflater, this) }

    private val toolbarTitleText: TextView?
        get() = binding.toolbar.children.firstOrNull { it.isVisible && it is TextView } as? TextView

    @DrawableRes
    private var iconAppBarBackRes: Int? = null
    private val localeProvider by lazy {
        if (isInEditMode) {
            LocaleProvider(ResourceProvider(context))
        } else {
            Dependencies.localeProvider
        }
    }

    init {
        setDefaults(attrs)
    }

    private val leaveQueueIcon: MenuItem
        get() = binding.toolbar.menu.findItem(R.id.leave_queue_button)

    private fun setDefaults(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.AppBarView) {
            iconAppBarBackRes = getTypedArrayResId(
                this,
                R.styleable.AppBarView_backIcon,
                R.attr.gliaIconAppBarBack
            ).also {
                binding.toolbar.setNavigationIcon(it)
            }
            binding.endButton.setLocaleText(R.string.general_end)
            binding.endButton.setLocaleContentDescription(R.string.android_app_bar_end_engagement_accessibility_label)

            val titleColorRes = getTypedArrayResId(
                this,
                R.styleable.AppBarView_lightTint,
                R.attr.gliaChatHeaderTitleTintColor
            )
            binding.title.setTextColor(getColorCompat(titleColorRes))

            val homeButtonTintColor = getTypedArrayResId(
                this,
                R.styleable.AppBarView_lightTint,
                R.attr.gliaChatHeaderHomeButtonTintColor
            )
            binding.toolbar.navigationIcon?.setTint(getColorCompat(homeButtonTintColor))
            binding.toolbar.setLocaleNavigationContentDescription(R.string.android_app_bar_nav_up_accessibility)
            val backgroundTintList = getTypedArrayResId(
                this,
                R.styleable.AppBarView_android_backgroundTint,
                R.attr.gliaBrandPrimaryColor
            )

            setBackgroundColor(getColorCompat(backgroundTintList))

            Utils.getTypedArrayStringValue(this, R.styleable.AppBarView_titleText)?.also {
                binding.title.text = it
            }
        }
        context.withStyledAttributes(attrs, R.styleable.GliaView) {
            Utils.getFont(this, context)?.also { binding.title.typeface = it }

            val leaveIconTint = getTypedArrayResId(
                this,
                R.styleable.GliaView_chatHeaderExitQueueButtonTintColor,
                R.attr.gliaChatHeaderExitQueueButtonTintColor
            )
            leaveQueueIcon.icon?.setTint(getColorCompat(leaveIconTint))
            leaveQueueIcon.title = localeProvider.getString(R.string.general_close)
        }
    }

    fun setTheme(uiTheme: UiTheme?) {
        if (uiTheme == null) return
        // icons
        uiTheme.iconAppBarBack?.also(binding.toolbar::setNavigationIcon)
        uiTheme.iconLeaveQueue?.also(leaveQueueIcon::setIcon)

        // colors
        val brandPrimaryColor = uiTheme.brandPrimaryColor?.let(::getColorCompat)
        val baseLightColor = uiTheme.baseLightColor?.let(::getColorCompat)
        val systemNegativeColor = uiTheme.systemNegativeColor?.let(::getColorCompat)
        val exitQueueButtonColor = uiTheme.gliaChatHeaderExitQueueButtonTintColor?.let(::getColorCompat) ?: baseLightColor
        val chatHeaderTitleColor = uiTheme.gliaChatHeaderTitleTintColor?.let(::getColorCompat)
        val chatHeaderHomeButtonColor = uiTheme.gliaChatHeaderHomeButtonTintColor?.let(::getColorCompat)
        val textFont = uiTheme.fontRes?.let(::getFontCompat)

        chatHeaderHomeButtonColor?.also { binding.toolbar.setNavigationIconTint(it) }
        brandPrimaryColor?.also { setBackgroundColor(it) }

        leaveQueueIcon.applyIconColorTheme(exitQueueButtonColor)
        binding.title.applyTextTheme(chatHeaderTitleColor, textFont)
        binding.endButton.applyButtonTheme(
            backgroundColor = systemNegativeColor,
            textColor = baseLightColor,
            textFont = textFont
        )
    }

    fun hideBackButton() {
        binding.toolbar.navigationIcon = null
    }

    fun showBackButton() {
        iconAppBarBackRes?.let { binding.toolbar.setNavigationIcon(it) }
    }

    fun setTitle(title: LocaleString?) {
        binding.title.setText(title)
    }

    fun setVisibility(visibility: Boolean = true) {
        isVisible = visibility
    }

    fun showXButton() {
        binding.endButton.isGone = true
        leaveQueueIcon.isVisible = true
    }

    fun showEndButton() {
        binding.endButton.isVisible = true
        leaveQueueIcon.isVisible = false
    }

    fun hideXAndEndButton() {
        binding.endButton.isGone = true
        leaveQueueIcon.isVisible = false
    }

    fun setOnBackClickedListener(onBackClicked: OnBackClicked) {
        binding.toolbar.setNavigationOnClickListener { onBackClicked() }
    }

    fun setOnXClickedListener(onXClicked: OnXClicked) {
        binding.toolbar.setOnMenuItemClickListener {
            onXClicked()
            true
        }
    }

    fun setOnEndChatClickedListener(onEndChatClicked: OnEndChatClicked) {
        binding.endButton.setOnClickListener { onEndChatClicked() }
    }

    fun hideLeaveButtons() {
        binding.endButton.isGone = true
        leaveQueueIcon.isVisible = false
    }

    internal fun resetTheme() {
        val textAttrs = intArrayOf(com.google.android.material.R.attr.textAppearanceHeadline2)
        context.obtainStyledAttributes(textAttrs).apply {
            toolbarTitleText?.also { TextViewCompat.setTextAppearance(it, getResourceId(0, 0)) }
            recycle()
        }
    }

    internal fun applyHeaderTheme(headerTheme: HeaderTheme?) {
        headerTheme?.apply {
            applyColorTheme(background?.fill)
            backButton?.iconColor?.also { binding.toolbar.setNavigationIconTint(it.primaryColor) }
            text?.also(::applyTitleTheme)
            closeButton?.iconColor?.also { leaveQueueIcon.icon?.setTintCompat(it.primaryColor) }
            endButton?.also { binding.endButton.applyButtonTheme(it) }
        }
    }

    private fun applyTitleTheme(textTheme: TextTheme) {
        if (textTheme.textColor != null && toolbarTitleText == null) {
            binding.toolbar.setTitleTextColor(textTheme.textColor.primaryColor)
            return
        }

        toolbarTitleText?.also { it.applyTextTheme(textTheme) }
    }

    fun interface OnBackClicked {
        operator fun invoke()
    }

    fun interface OnXClicked {
        operator fun invoke()
    }

    fun interface OnEndChatClicked {
        operator fun invoke()
    }
}
