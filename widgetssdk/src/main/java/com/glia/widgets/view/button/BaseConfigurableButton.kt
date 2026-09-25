package com.glia.widgets.view.button

import android.content.Context
import android.util.AttributeSet
import com.glia.widgets.R
import com.google.android.material.button.MaterialButton

/**
 * Base for the SDK's themed buttons. Each subclass names the theme attribute that carries its style,
 * and everything visual comes from there - the composed Glia theme resolves it, so there is nothing
 * left to apply in code.
 *
 * @hide
 */
abstract class BaseConfigurableButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.buttonBarNeutralButtonStyle
) : MaterialButton(context, attrs, defStyleAttr)
