package com.glia.widgets.view.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.AppBarBinding
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.unifiedui.exstensions.*
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.android.material.appbar.AppBarLayout
import kotlin.properties.Delegates

class AppBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
) : AppBarLayout(context, attrs, defStyleAttr) {
    private var binding: AppBarBinding by Delegates.notNull()

    private val toolbarTitleText: TextView?
        get() = binding.toolbar.children.firstOrNull { it.isVisible && it is TextView } as? TextView

    init {
        binding = AppBarBinding.inflate(LayoutInflater.from(context), this)
        setDefaults(attrs)
    }

    private val leaveQueueIcon: MenuItem
        get() = binding.toolbar.menu.findItem(R.id.leave_queue_button)

    private fun setDefaults(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.AppBarView) {
            binding.toolbar.setNavigationIcon(
                getTypedArrayResId(
                    this, R.styleable.AppBarView_backIcon, R.attr.gliaIconAppBarBack
                )
            )

            val titleColorRes = getTypedArrayResId(
                this, R.styleable.AppBarView_lightTint, R.attr.gliaChatHeaderTitleTintColor
            )
            binding.title.setTextColor(getColorCompat(titleColorRes))

            val homeButtonTintColor = getTypedArrayResId(
                this, R.styleable.AppBarView_lightTint, R.attr.gliaChatHeaderHomeButtonTintColor
            )
            binding.toolbar.navigationIcon?.setTint(getColorCompat(homeButtonTintColor))

            val backgroundTintList = getTypedArrayResId(
                this, R.styleable.AppBarView_android_backgroundTint, R.attr.gliaBrandPrimaryColor
            )
            binding.toolbar.backgroundTintList = getColorStateListCompat(backgroundTintList)

            Utils.getTypedArrayStringValue(this, R.styleable.AppBarView_titleText)?.also {
                binding.title.text = it
            }
        }
        context.withStyledAttributes(attrs, R.styleable.GliaView) {
            Utils.getFont(this, context)?.also { binding.title.typeface = it }

            val leaveIconTint = getTypedArrayResId(
                this, R.styleable.GliaView_chatHeaderExitQueueButtonTintColor,
                R.attr.gliaChatHeaderExitQueueButtonTintColor
            )
            leaveQueueIcon.icon?.setTint(getColorCompat(leaveIconTint))
        }
    }

    fun setTheme(uiTheme: UiTheme?) {
        if (uiTheme == null) return
        // icons
        uiTheme.iconAppBarBack?.also(binding.toolbar::setNavigationIcon)
        uiTheme.iconLeaveQueue?.also(leaveQueueIcon::setIcon)

        // colors
        uiTheme.brandPrimaryColor?.let(::getColorStateListCompat)?.also {
            binding.toolbar.backgroundTintList = it
        }
        (uiTheme.gliaChatHeaderExitQueueButtonTintColor ?: uiTheme.baseLightColor)?.also {
            leaveQueueIcon.icon?.setTintCompat(it)
        }

        uiTheme.gliaChatHeaderTitleTintColor?.let(::getColorStateListCompat)
            ?.also(binding.title::setTextColor)

        uiTheme.gliaChatHeaderHomeButtonTintColor?.let(::getColorCompat)?.also {
            binding.toolbar.navigationIcon?.setTint(it)
        }
        binding.endButton.setTheme(uiTheme)

        uiTheme.fontRes?.let { binding.title.typeface = getFontCompat(it) }
    }

    fun setTitle(title: String?) {
        binding.title.text = title
    }

    fun showToolbar() {
        isVisible = true
    }

    fun showXButton() {
        binding.endButton.isGone = true
        leaveQueueIcon.isVisible = true
    }

    fun showEndButton() {
        binding.endButton.isVisible = true
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

    private var saveItem: MenuItem? = null
    private var shareItem: MenuItem? = null

    fun setMenuImagePreview() {
        binding.toolbar.inflateMenu(R.menu.menu_file_preview)
        val menu = binding.toolbar.menu
        saveItem = menu.findItem(R.id.save_item)
        shareItem = menu.findItem(R.id.share_item)
    }

    fun setImagePreviewButtonListener(listener: OnImagePreviewMenuListener) {
        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.share_item) {
                listener.onShareClicked()
            } else if (itemId == R.id.save_item) {
                listener.onDownloadClicked()
            }
            true
        }
    }

    fun setImagePreviewButtonsVisible(saveItem: Boolean, shareItem: Boolean) {
        this.saveItem!!.isVisible = saveItem
        this.shareItem!!.isVisible = shareItem
    }

    internal fun applyHeaderTheme(headerTheme: HeaderTheme?) {
        headerTheme?.apply {
            binding.toolbar.applyColorTheme(background?.fill)
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

    interface OnImagePreviewMenuListener {
        fun onShareClicked()
        fun onDownloadClicked()
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