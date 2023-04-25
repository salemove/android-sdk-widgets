package com.glia.widgets.view

import android.content.Context
import android.content.res.TypedArray
import android.os.CountDownTimer
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.button.GliaPositiveButton
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.glia.widgets.view.unifiedui.extensions.*
import com.glia.widgets.view.unifiedui.extensions.applyButtonTheme
import com.glia.widgets.view.unifiedui.extensions.applyLayerTheme
import com.glia.widgets.view.unifiedui.extensions.applyTextTheme
import com.glia.widgets.view.unifiedui.extensions.layoutInflater
import com.glia.widgets.view.unifiedui.extensions.applyImageColorTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme

/**
 * A view for displaying the visitor code to the visitor.
 *
 * This view should not be instantiated directly - neither programmatically nor inside your XML layouts.
 * Use [CallVisualizer.createVisitorCodeView] to create an instance of this view.
 */
class VisitorCodeView internal constructor(
    context: Context
) : FrameLayout(MaterialThemeOverlay.wrap(context, null, 0, R.style.Application_Glia_Chat), null, 0), VisitorCodeContract.View {
    private lateinit var controller: VisitorCodeContract.Controller
    private var theme: UiTheme? = null

    private var timer : CountDownTimer? = null

    private var successContainer: View
    private var failureContainer: View
    private var successTitle: TextView
    private var failureTitle: TextView
    private var charCodeView: CharCodeView
    private var progressBar: ProgressBar
    private var refreshButton: GliaPositiveButton
    private var closeButton: AppCompatImageButton
    private var logoView: ImageView

    init {
        layoutInflater.inflate(R.layout.visitor_code_view, this, true)
        successContainer = findViewById(R.id.success_container)
        failureContainer = findViewById(R.id.failure_container)
        successTitle = findViewById(R.id.success_title_view)
        failureTitle = findViewById(R.id.failure_title)
        charCodeView = findViewById(R.id.codeView)
        progressBar = findViewById(R.id.progress_bar)
        refreshButton = findViewById(R.id.failure_refresh_button)
        refreshButton.setOnClickListener { controller.onLoadVisitorCode() }
        closeButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener { controller.onCloseButtonClicked() }
        logoView = findViewById(R.id.logo_view)
        readTypedArray()
        applyRemoteThemeConfig(Dependencies.getGliaThemeManager().theme)

        setController(Dependencies.getControllerFactory().visitorCodeController)
    }

    override fun notifySetupComplete() {
        controller.onLoadVisitorCode()
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

    override fun setTimer(duration: Long) {
        Logger.d(TAG, "Setting visitor code timeout to $duration")
        timer = object : CountDownTimer(duration, duration) {
            override fun onTick(p0: Long) {
                // no-op
            }

            override fun onFinish() {
                Logger.d(TAG, "Reloading Visitor Code")
                controller.onLoadVisitorCode()
            }
        }.start()
    }

    override fun destroyTimer() {
        Logger.d(TAG, "Dismissing Visitor Code reload timer")
        timer?.cancel()
        timer = null
    }

    override fun setController(controller: VisitorCodeContract.Controller) {
        this.controller = controller
        this.controller.setView(this)
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        val typedArrayTheme = Utils.getThemeFromTypedArray(typedArray, this.context)
        val runtimeGlobalTheme = Dependencies.getSdkConfigurationManager()?.uiTheme
        theme = if (typedArrayTheme != null && runtimeGlobalTheme != null) {
            Utils.getFullHybridTheme(runtimeGlobalTheme, typedArrayTheme)
        } else runtimeGlobalTheme ?: typedArrayTheme
        applyRuntimeThemeConfig(theme)
    }

    override fun startLoading() {
        runOnUi {
            showProgressBar(true)
            showSuccess()
            successTitle.contentDescription = context.getString(R.string.glia_visitor_code_loading)
            successTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
            closeButton.contentDescription = context.getString(R.string.glia_chat_alert_dialog_close_content_description)
        }
    }

    override fun showError(throwable: Throwable) {
        Logger.e(TAG, throwable.message, throwable)
        runOnUi {
            showProgressBar(false)
            showFailure()
        }
    }

    private fun showSuccess() {
        successContainer.visibility = VISIBLE
        failureContainer.visibility = GONE
    }

    private fun showFailure() {
        failureContainer.visibility = VISIBLE
        successContainer.visibility = GONE
    }

    override fun showVisitorCode(visitorCode: VisitorCode) {
        runOnUi {
            showProgressBar(false)
            successTitle.contentDescription = context.getString(R.string.glia_visitor_code_content_description, visitorCode.code.separateStringWithSymbol("-"))
            successTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
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

    private fun applyRemoteThemeConfig(theme: UnifiedTheme?) {
        theme?.callVisualizerTheme.apply {
            applyLayerTheme(this?.visitorCodeTheme?.background)
            successTitle.applyTextTheme(this?.visitorCodeTheme?.title)
            failureTitle.applyTextTheme(this?.visitorCodeTheme?.title)
            progressBar.applyProgressColorTheme(this?.visitorCodeTheme?.loadingProgressBar)
            closeButton.applyImageColorTheme(this?.visitorCodeTheme?.closeButtonColor)
            refreshButton.applyButtonTheme(this?.visitorCodeTheme?.refreshButton)
        }
    }

    private fun applyRuntimeThemeConfig(theme: UiTheme?) {
        if (theme == null) {
            Logger.d(TAG, "UiTheme is null!")
            return
        }
        charCodeView.applyRuntimeTheme(theme)

        val brandPrimaryColor = theme.brandPrimaryColor?.let(::getColorCompat)
        val baseLightColor = theme.baseLightColor?.let(::getColorCompat)
        val baseNormalColor = theme.baseNormalColor?.let(::getColorCompat)
        val baseShadeColor = theme.baseShadeColor?.let(::getColorCompat)
        val baseDarkColor = theme.baseDarkColor?.let(::getColorCompat)
        val fontFamily = theme.fontRes?.let(::getFontCompat)

        applyLayerTheme(
            backgroundColor = baseLightColor
        )
        successTitle.applyTextTheme(
            textColor = baseDarkColor,
            textFont = fontFamily
        )
        failureTitle.applyTextTheme(
            textColor = baseDarkColor,
            textFont = fontFamily
        )
        refreshButton.applyButtonTheme(
            backgroundColor = brandPrimaryColor,
            textColor = baseLightColor,
            textFont = fontFamily
        )
        logoView.apply {
            isVisible = theme.whiteLabel?.not() ?: true
            applyImageColorTheme(baseShadeColor)
        }
        progressBar.applyProgressColorTheme(brandPrimaryColor)
        closeButton.applyImageColorTheme(baseNormalColor)
    }
}
