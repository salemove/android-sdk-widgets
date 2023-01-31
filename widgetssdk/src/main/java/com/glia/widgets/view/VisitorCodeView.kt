package com.glia.widgets.view

import android.content.Context
import android.util.AttributeSet
import kotlin.jvm.JvmOverloads
import android.widget.FrameLayout
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository
import android.view.LayoutInflater
import android.widget.TextView
import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.R
import com.glia.widgets.helper.Logger
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import java.lang.Exception
import java.lang.IllegalStateException

class VisitorCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val TAG = VisitorCodeView::class.java.simpleName
    private var visitorCodeRepository: VisitorCodeRepository? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.visitor_code_view, this, true)
        // TODO: apply the theme?
    }

    fun setVisitorCodeRepository(visitorCodeRepository: VisitorCodeRepository) {
        this.visitorCodeRepository = visitorCodeRepository
        loadNewVisitorCode()
    }

    private fun loadNewVisitorCode() {
        if (visitorCodeRepository == null) {
            val error: Exception = IllegalStateException("Missing visitor code repository")
            notifyOfError("Internal error, invalid view setup", error)
            return
        }

        visitorCodeRepository?.getVisitorCode { visitorCode, error ->
            if (error != null) {
                notifyOfError("Failed to load Visitor Code", error)
                return@getVisitorCode
            }
            if (visitorCode == null || visitorCode.code == null) {
                val error: Exception = IllegalStateException("Empty Visitor Code returned by the Glia Core")
                notifyOfError("Failed to load Visitor Code", error)
                return@getVisitorCode
            }

            notifyOfNewVisitorCode(visitorCode)
        }
    }

    private fun notifyOfError(publicErrorMessage: String, exception: Throwable?) {
        Logger.e(TAG, publicErrorMessage, exception)

        context.run {  }
        findViewById<VisitorCodeCodeView>(R.id.codeView).apply {
            setText("ERROR") // Will be replace and improved in the next ticket MOB-1823
        }
    }

    private fun notifyOfNewVisitorCode(visitorCode: VisitorCode) {
        findViewById<VisitorCodeCodeView>(R.id.codeView).apply {
            setText(visitorCode.code)
        }
    }

    // TODO: make sure it is applied!!!
    internal fun applyBadgeTheme(alertTheme: AlertTheme?) {


//        alertTheme?.backgroundColor?.primaryColorStateList?.let(decorView::setBackgroundTintList)
//            ?: tintRes?.let { ContextCompat.getColorStateList(dialog.context, it) }?.also {
//                decorView.backgroundTintList = it
//            }
//
//
//        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
//        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }
//
//        findViewById<TextView>(R.id.dialog_title_view).apply {
//            setText(title)
//            baseDarkColor?.also(::setTextColor)
//            fontFamily?.also(::setTypeface)
//            Dialogs.alertTheme?.title.also(::applyTextTheme)
//        }
//        findViewById<TextView>(R.id.dialog_message_view).apply {
//            setText(message)
//            baseDarkColor?.also(::setTextColor)
//            fontFamily?.also(::setTypeface)
//            Dialogs.alertTheme?.message.also(::applyTextTheme)
//        }
//        findViewById<ImageView>(R.id.logo_view).apply {
//            isVisible = theme.whiteLabel ?: false
//            theme.baseShadeColor?.let { ContextCompat.getColorStateList(context, it) }
//                ?.also(::setImageTintList)
//        }
//
//
//        theme?.apply {
//            background?.also(::applyLayerTheme)
//            textColor.also(::applyTextColorTheme)
//            textStyle?.also { typeface = Typeface.create(typeface, it) }
//            textSize?.also { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
//        }
    }
}