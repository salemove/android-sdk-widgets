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
import com.glia.widgets.databinding.AppBarBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.applyGliaThemeFont
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.gliaAttrResourceId
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
    defStyleAttr: Int = 0
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
        iconAppBarBackRes = context.gliaAttrResourceId(
            R.attr.gliaIconAppBarBack,
            R.drawable.ic_baseline_arrow_back
        ).also {
            binding.toolbar.setNavigationIcon(it)
        }
        binding.endButton.setLocaleText(R.string.general_end)
        binding.endButton.setLocaleContentDescription(R.string.android_app_bar_end_engagement_accessibility_label)

        val titleColorRes = context.gliaAttrResourceId(
            R.attr.gliaChatHeaderTitleTintColor,
            R.color.glia_light_color
        )
        binding.title.setTextColor(getColorCompat(titleColorRes))

        val homeButtonTintColor = context.gliaAttrResourceId(
            R.attr.gliaChatHeaderHomeButtonTintColor,
            R.color.glia_light_color
        )
        binding.toolbar.navigationIcon?.setTint(getColorCompat(homeButtonTintColor))
        binding.toolbar.setLocaleNavigationContentDescription(R.string.android_app_bar_nav_up_accessibility)

        val backgroundColor = context.gliaAttrResourceId(
            R.attr.gliaBrandPrimaryColor,
            R.color.glia_primary_color
        )
        setBackgroundColor(getColorCompat(backgroundColor))

        // `titleText` is only ever set as `tools:titleText`, so this read exists purely so the four
        // layouts embedding an app bar render a title in the layout editor.
        context.withStyledAttributes(attrs, R.styleable.AppBarView) {
            getString(R.styleable.AppBarView_titleText)?.also {
                binding.title.text = it
            }
        }

        val leaveIconTint = context.gliaAttrResourceId(
            R.attr.gliaChatHeaderExitQueueButtonTintColor,
            R.color.glia_light_color
        )
        leaveQueueIcon.icon?.setTint(getColorCompat(leaveIconTint))
        leaveQueueIcon.title = localeProvider.getString(R.string.general_close)

        // A `textAppearance` cannot express "the typeface the theme asks for", so this is the one
        // value the app bar still has to resolve in code.
        context.applyGliaThemeFont(binding.title, binding.endButton)
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

    fun setOnEndClickListener(onEndClicked: OnEndClicked) {
        binding.endButton.setOnClickListener { onEndClicked() }
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

    fun interface OnEndClicked {
        operator fun invoke()
    }
}
