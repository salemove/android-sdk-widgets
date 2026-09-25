package com.glia.widgets.view.textview

import android.content.Context
import android.util.AttributeSet
import com.glia.widgets.R
import com.google.android.material.textview.MaterialTextView

/**
 * Base for the SDK's themed text views. Each subclass names the theme attribute that carries its
 * text appearance; the composed Glia theme resolves it, so there is nothing left to apply in code.
 *
 * @hide
 */
abstract class BaseConfigurableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textAppearanceBody1,
    defStyleRes: Int = R.style.Application_Glia_Body
) : MaterialTextView(context, attrs, defStyleAttr, defStyleRes)
