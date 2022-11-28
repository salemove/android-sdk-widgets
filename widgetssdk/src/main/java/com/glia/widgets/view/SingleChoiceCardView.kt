package com.glia.widgets.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.ColorInt
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.SingleChoiceCardViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.unifiedui.exstensions.*
import com.glia.widgets.view.unifiedui.theme.chat.ResponseCardTheme
import com.google.android.material.button.MaterialButton
import kotlin.properties.Delegates

class SingleChoiceCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {
    private var onOptionClickedListener: OnOptionClickedListener? = null
    private var binding: SingleChoiceCardViewBinding by Delegates.notNull()
    private val bgDrawable: GradientDrawable by lazy { GradientDrawable() }
    private val responseCardTheme: ResponseCardTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.chatTheme?.responseCard
    }

    init {
        binding = SingleChoiceCardViewBinding.inflate(layoutInflater, this)

        bgDrawable.color =
            getColorStateListCompat(Utils.getAttrResourceId(context, R.attr.gliaBaseLightColor))

        setStroke(getColorCompat(Utils.getAttrResourceId(context, R.attr.gliaBrandPrimaryColor)))

        bgDrawable.cornerRadius = resources.getDimension(R.dimen.glia_medium)
        orientation = VERTICAL
        setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
        val verticalMargin = resources.getDimensionPixelSize(R.dimen.glia_pre_large)
        background = bgDrawable
        updatePadding(top = verticalMargin, bottom = verticalMargin)
    }

    private fun setStroke(
        @ColorInt color: Int,
        strokeSize: Int = resources.getDimensionPixelSize(R.dimen.glia_px)
    ) {
        bgDrawable.setStroke(strokeSize, color)
    }

    fun setData(
        id: String,
        imageUrl: String?,
        content: String,
        options: List<SingleChoiceOption>,
        selectedIndex: Int?,
        theme: UiTheme,
        adapterPosition: Int
    ) {
        setupCardView(theme)
        setupImage(imageUrl)
        setupText(content, theme)
        setupButtons(id, options, selectedIndex, theme, adapterPosition)
    }

    fun setOnOptionClickedListener(onOptionClickedListener: OnOptionClickedListener?) {
        this.onOptionClickedListener = onOptionClickedListener
    }

    fun interface OnOptionClickedListener {
        fun onClicked(id: String?, indexInList: Int, indexOfOption: Int)
    }

    private fun setupCardView(theme: UiTheme) {

        responseCardTheme?.background?.fill?.also {
            if (it.isGradient) {
                bgDrawable.colors = it.valuesArray
            } else {
                bgDrawable.setColor(it.primaryColor)
            }
        } ?: theme.baseLightColor?.also { bgDrawable.setColor(getColorCompat(it)) }

        responseCardTheme?.background?.stroke?.also(::setStroke)
            ?: theme.brandPrimaryColor?.also { setStroke(getColorCompat(it)) }
    }

    private fun setupImage(imageUrl: String?) {
        binding.image.apply {
            this.isVisible = imageUrl != null
            load(imageUrl)
        }
    }

    private fun setupText(content: String, theme: UiTheme) {
        binding.contentView.text = content
        binding.contentView.setTheme(theme)
        binding.contentView.applyTextTheme(responseCardTheme?.text, true)
    }

    private fun composeButton(
        text: String,
        isEnabled: Boolean,
        isSelected: Boolean,
        uiTheme: UiTheme,
        onClickListener: OnClickListener?
    ): MaterialButton {
        val styleResId = Utils.getAttrResourceId(this.context, R.attr.buttonBarNeutralButtonStyle)
        return MaterialButton(ContextThemeWrapper(this.context, styleResId), null, 0).also {
            it.id = View.generateViewId()
            it.text = text
            it.isEnabled = isEnabled
            it.isSelected = isSelected
            onClickListener?.also(it::setOnClickListener)

            uiTheme.fontRes?.let(::getFontCompat)?.also(it::setTypeface)

            when {
                isSelected -> {
                    uiTheme.botActionButtonSelectedBackgroundColor?.let(::getColorStateListCompat)
                        ?.also(it::setBackgroundTintList)
                    uiTheme.botActionButtonSelectedTextColor?.let(::getColorStateListCompat)
                        ?.also(it::setTextColor)

                    responseCardTheme?.option?.selected.also(it::applyButtonTheme)
                }
                !isEnabled -> {
                    it.strokeWidth = resources.getDimensionPixelSize(R.dimen.glia_px)
                    uiTheme.baseShadeColor?.let(::getColorStateListCompat)?.also(it::setStrokeColor)

                    responseCardTheme?.option?.disabled.also(it::applyButtonTheme)
                }
                else -> {
                    uiTheme.botActionButtonBackgroundColor?.let(::getColorStateListCompat)
                        ?.also(it::setBackgroundTintList)
                    uiTheme.botActionButtonTextColor?.let(::getColorStateListCompat)
                        ?.also(it::setTextColor)

                    responseCardTheme?.option?.normal.also(it::applyButtonTheme)
                }
            }
        }
    }

    private fun setupButtons(
        id: String,
        options: List<SingleChoiceOption>,
        selectedIndex: Int?,
        theme: UiTheme,
        adapterPosition: Int
    ) {
        val horizontalMargin = resources.getDimensionPixelOffset(R.dimen.glia_large)
        val topMargin = resources.getDimensionPixelOffset(R.dimen.glia_medium)
        for (index in options.indices) {

            val onClickListener = onOptionClickedListener?.run {
                OnClickListener { onClicked(id, adapterPosition, index) }
            }

            val button = composeButton(
                text = options[index].text,
                isEnabled = selectedIndex == null,
                isSelected = selectedIndex == index,
                uiTheme = theme,
                onClickListener = onClickListener
            )

            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            params.setMargins(horizontalMargin, topMargin, horizontalMargin, 0)
            addView(button, params)

        }
    }
}