package com.glia.widgets.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.glia.widgets.R
import com.glia.widgets.helper.getDrawableCompat
import com.glia.widgets.helper.gliaAttrResourceId

/**
 * @hide
 */
class OutlinedOptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val iconView: ImageView
    private val titleView: TextView
    private val captionView: TextView

    init {
        isClickable = true
        isFocusable = true
        addRippleEffect()
        val view: View = View.inflate(context, R.layout.outlined_option_view, this)
        iconView = view.findViewById(R.id.icon_view)
        titleView = view.findViewById(R.id.title_view)
        captionView = view.findViewById(R.id.caption_view)
        readTypedArray(attrs)
    }

    private fun readTypedArray(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.OutlinedOptionView) {
            if (hasValue(R.styleable.OutlinedOptionView_icon)) {
                iconView.setImageResource(getResourceId(R.styleable.OutlinedOptionView_icon, 0))
            }
            if (hasValue(R.styleable.OutlinedOptionView_title)) {
                titleView.text = getString(R.styleable.OutlinedOptionView_title)
            }
            if (hasValue(R.styleable.OutlinedOptionView_caption)) {
                captionView.text = getString(R.styleable.OutlinedOptionView_caption)
            }
        }
    }

    private fun addRippleEffect() {
        val ripple = context.gliaAttrResourceId(android.R.attr.selectableItemBackground) ?: return
        foreground = getDrawableCompat(ripple)
    }
}
