package com.glia.widgets.view.dialog.base

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.di.Dependencies
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.helper.setText
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.AlertDialogConfiguration
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.google.android.material.button.MaterialButton

internal abstract class DialogViewInflater<T : DialogViewBinding<out ViewBinding>, R : DialogPayload>(
    binding: T,
    themeWrapper: AlertDialogConfiguration,
    payload: R
) {

    val view: View = binding.root

    init {
        initialSetup(binding, themeWrapper, payload)
    }

    private fun initialSetup(binding: T, configuration: AlertDialogConfiguration, payload: R) {
        val alertTheme = configuration.theme.alertTheme
        view.applyColorTheme(alertTheme?.backgroundColor)
        setupText(binding.titleTv, payload.title, alertTheme?.title, configuration.properties.typeface)
        setup(binding, configuration, payload)
    }

    abstract fun setup(binding: T, configuration: AlertDialogConfiguration, payload: R)

    private fun setupTypeface(tv: TextView, typeface: Typeface?) {
        typeface?.also { tv.typeface = it }
    }

    protected fun setupText(tv: TextView, text: LocaleString, textTheme: TextTheme?, typeface: Typeface?) {
        tv.apply {
            this.setText(text)
            applyTextTheme(textTheme)
            setupTypeface(this, typeface)
        }
    }

    protected fun setupButton(btn: MaterialButton, text: LocaleString, btnTheme: ButtonTheme?, typeface: Typeface?, onClickListener: View.OnClickListener) {
        btn.apply {
            setText(text)
            applyButtonTheme(btnTheme)
            setupTypeface(this, typeface)
            setOnClickListener(onClickListener)
        }
    }

    protected fun setupButton(btn: MaterialButton, link: Link, btnTheme: ButtonTheme?, typeface: Typeface?, onClickListener: View.OnClickListener) {
        btn.apply {
            setText(link.title) { upToDateTitle ->
                this.text = upToDateTitle

                val localeManager = Dependencies.localeProvider
                val upToDateUrl = localeManager.getString(link.url)

                this.isVisible = upToDateTitle.isNotBlank() && upToDateUrl.isNotBlank()
            }
            applyButtonTheme(btnTheme)
            setupTypeface(this, typeface)
            setOnClickListener(onClickListener)
        }
    }
}
