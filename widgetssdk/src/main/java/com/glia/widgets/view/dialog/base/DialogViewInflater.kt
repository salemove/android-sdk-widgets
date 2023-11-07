package com.glia.widgets.view.dialog.base

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.AlertThemeWrapper
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.android.material.button.MaterialButton

internal abstract class DialogViewInflater<T : DialogViewBinding<out ViewBinding>, R : DialogPayload>(
    binding: T,
    themeWrapper: AlertThemeWrapper,
    payload: R
) {

    val view: View = binding.root

    init {
        initialSetup(binding, themeWrapper, payload)
    }

    private fun initialSetup(binding: T, themeWrapper: AlertThemeWrapper, payload: R) {
        val alertTheme = themeWrapper.theme
        view.applyColorTheme(alertTheme.backgroundColor)
        setupText(binding.titleTv, payload.title, alertTheme.title, themeWrapper.typeface)
        setup(binding, themeWrapper, payload)
    }

    abstract fun setup(binding: T, themeWrapper: AlertThemeWrapper, payload: R)

    private fun setupTypeface(tv: TextView, typeface: Typeface?) {
        typeface?.also { tv.typeface = it }
    }

    protected fun setupText(tv: TextView, text: String, textTheme: TextTheme?, typeface: Typeface?) {
        tv.apply {
            this.text = text
            applyTextTheme(textTheme)
            setupTypeface(this, typeface)
        }
    }

    protected fun setupButton(btn: MaterialButton, text: String, btnTheme: ButtonTheme?, typeface: Typeface?, onClickListener: View.OnClickListener) {
        btn.apply {
            this.text = text
            applyButtonTheme(btnTheme)
            setupTypeface(this, typeface)
            setOnClickListener(onClickListener)
        }
    }
}
