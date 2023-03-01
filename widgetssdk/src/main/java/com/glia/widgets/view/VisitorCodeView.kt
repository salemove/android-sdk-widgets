package com.glia.widgets.view

import android.content.Context
import android.content.res.TypedArray
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.VisitorCodeRepository
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.unifiedui.exstensions.applyLayerTheme
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.exstensions.layoutInflater
import com.google.android.material.theme.overlay.MaterialThemeOverlay

/**
 * TODO: add doc in MOB-1816
 * TODO: N.B: this view is not expected to be used by integrator or view XML
 * TODO: Test what will happen if I add it to XML regardless
 */
class VisitorCodeView internal constructor(
    context: Context
) : FrameLayout(MaterialThemeOverlay.wrap(context, null, 0, R.style.Application_Glia_Chat), null, 0), VisitorCodeContract.View {
    private val TAG = VisitorCodeView::class.java.simpleName
    private var controller: VisitorCodeContract.Controller? = null
    private var visitorCodeRepository: VisitorCodeRepository? = null
    private var theme: UiTheme? = null

    private var titleView: TextView
    private var charCodeView: CharCodeView
    private var progressBar: ProgressBar
    private var closeButton: AppCompatImageButton

    init {
        layoutInflater.inflate(R.layout.visitor_code_view, this, true)
        titleView = findViewById(R.id.title_view)
        charCodeView = findViewById(R.id.codeView)
        progressBar = findViewById(R.id.progress_bar)
        closeButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener { controller?.onCloseButtonClicked() }
        readTypedArray()
        applyRemoteConfigTheme()
        setController(Dependencies.getControllerFactory().visitorCodeController)
    }

    internal fun setClosable(isClosable: Boolean) {
        closeButton.isVisible = isClosable
    }

    private fun readTypedArray() {
        context.withStyledAttributes(
            set = null,
            attrs = R.styleable.GliaView,
            defStyleAttr = 0
        ) {
            setDefaultTheme(this)
        }
    }

    override fun setController(controller: VisitorCodeContract.Controller?) {
        this.controller = controller
        this.controller?.setView(this)
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
        applyDialogTheme(theme)
    }

    fun setVisitorCodeRepository(visitorCodeRepository: VisitorCodeRepository) {
        this.visitorCodeRepository = visitorCodeRepository
        loadNewVisitorCode()
    }

    private fun loadNewVisitorCode() {
        notifyOfLoading()
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

    private fun notifyOfLoading() {
        runOnUi {
            showProgressBar(true)
        }
    }

    private fun notifyOfError(publicErrorMessage: String, exception: Throwable?) {
        Logger.e(TAG, publicErrorMessage, exception)

        runOnUi {
            showProgressBar(false)
            charCodeView.setText("ERROR") // Will be replace and improved in the next ticket MOB-1823
        }
    }

    private fun notifyOfNewVisitorCode(visitorCode: VisitorCode) {
        runOnUi {
            showProgressBar(false)
            charCodeView.setText(visitorCode.code)
        }
    }

    private fun showProgressBar(show: Boolean) {
        if (show) {
            charCodeView.alpha = 0f
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
            charCodeView.animate().alpha(1f).duration = 200
        }
    }

    private fun runOnUi(function: () -> Unit) {
        post(function)
    }

    private fun applyRemoteConfigTheme() {
        Dependencies.getGliaThemeManager().theme?.callVisualizerTheme.apply {
            applyLayerTheme(this?.visitorCodeTheme?.background)
            titleView.applyTextTheme(this?.visitorCodeTheme?.title)
            progressBar.indeterminateTintList = this?.visitorCodeTheme?.loadingProgressBar?.primaryColorStateList
        }
    }

    // TODO: make sure it is applied!!!
    private fun applyDialogTheme(theme: UiTheme?) {
        if (theme == null) {
            Logger.d(TAG, "UiTheme is null!")
            return
        }

        theme.baseDarkColor?.run {
            titleView.setTextColor(ContextCompat.getColor(context, this))
        }
        theme.fontRes?.run {
            titleView.typeface = ResourcesCompat.getFont(context, this)
        }
        theme.baseLightColor?.run {
            rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.glia_base_light_color))
        }
    }
}