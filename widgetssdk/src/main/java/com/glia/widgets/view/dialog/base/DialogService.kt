package com.glia.widgets.view.dialog.base

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.OTel
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.opentelemetry.api.trace.Span
import java.util.concurrent.atomic.AtomicReference

typealias DialogOnShowCallback = (AlertDialog) -> Unit
typealias DialogOnLayoutCallback = Dialog.() -> Unit

internal class DialogService(private val unifiedTheme: UnifiedTheme?) {
    fun showDialog(
        context: Context,
        theme: UiTheme,
        type: DialogType,
        cancelable: Boolean = false,
        onShow: DialogOnShowCallback? = null,
        onLayout: DialogOnLayoutCallback? = null
    ): AlertDialog {
        val verticalInset = context.resources.getDimensionPixelSize(R.dimen.glia_large_x_large)
        val view = DialogViewFactory(context, theme, unifiedTheme).createView(type)

        val alertDialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(cancelable)
            .setBackgroundInsetBottom(verticalInset)
            .setBackgroundInsetTop(verticalInset)
            .create()

        val spanRef = AtomicReference<Span>()

        alertDialog.setOnShowListener{
            OTel.newAppSpan(type.trace).startSpan().also(spanRef::set)
            onShow?.invoke(alertDialog)
        }
        alertDialog.setOnDismissListener{
            spanRef.get()?.end()
        }

        return alertDialog.also {
            it.show()
            onLayout?.invoke(it)
        }
    }

}
