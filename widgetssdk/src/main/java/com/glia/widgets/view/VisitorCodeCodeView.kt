package com.glia.widgets.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.glia.widgets.R

// TODO: rename to something reasonable
class VisitorCodeCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {

    }

    fun setText(text: String) {
        removeAllViews()
        text.forEach {
            addView(createCharSlotView(it))
        }
    }

    private fun createCharSlotView(character: Char): TextView {
        val charView = TextView(context)
        charView.setBackgroundResource(R.drawable.bg_char_slot)
        charView.text = character.toString()
        charView.gravity = TEXT_ALIGNMENT_CENTER
        return charView
    }
}