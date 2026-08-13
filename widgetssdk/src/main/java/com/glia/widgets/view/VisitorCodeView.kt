package com.glia.widgets.view

import android.content.Context
import android.content.res.ColorStateList
import android.os.CountDownTimer
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.R
import com.glia.widgets.callvisualizer.VisitorCodeContract
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.applyGliaThemeFont
import com.glia.widgets.helper.applyImageColorTheme
import com.glia.widgets.helper.combineStringWith
import com.glia.widgets.helper.gliaAttrBoolean
import com.glia.widgets.helper.gliaAttrColor
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.separateStringWithSymbol
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.helper.wrapWithGliaTheme
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyProgressColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.applyWhiteLabel
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import java.util.concurrent.Executor

/**
 * A view for displaying the visitor code to the visitor.
 *
 * This view should not be instantiated directly -
 *  neither programmatically nor inside your XML layouts.
 * Use [CallVisualizer.createVisitorCodeView] to create an instance of this view.
 */
internal class VisitorCodeView internal constructor(
    context: Context,
    private val uiThreadExecutor: Executor? = null
) : FrameLayout(context.wrapWithGliaTheme(), null, 0), VisitorCodeContract.View {
    private var controller: VisitorCodeContract.Controller? = null

    private var timer: CountDownTimer? = null

    private var successContainer: View
    private var failureContainer: View
    private var successTitle: TextView
    private var failureTitle: TextView
    private var charCodeView: CharCodeView
    private var progressBar: ProgressBar
    private var refreshButton: GliaPositiveButton
    private var closeButton: AppCompatImageButton
    private var logoView: ImageView
    private var logoText: TextView
    private var logoContainer: View
    private var localeProvider: LocaleProvider = Dependencies.localeProvider

    init {
        layoutInflater.inflate(R.layout.visitor_code_view, this, true)
        successContainer = findViewById(R.id.success_container)
        failureContainer = findViewById(R.id.failure_container)
        successTitle = findViewById(R.id.success_title_view)
        failureTitle = findViewById(R.id.failure_title)
        charCodeView = findViewById(R.id.codeView)
        progressBar = findViewById(R.id.progress_bar)
        refreshButton = findViewById(R.id.failure_refresh_button)
        refreshButton.setOnClickListener { controller?.onRefreshButtonClicked() }
        closeButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener { controller?.onCloseButtonClicked() }
        logoContainer = findViewById(R.id.logo_container)
        logoView = findViewById(R.id.logo_view)
        logoText = findViewById(R.id.powered_by_text)
        logoText.setLocaleText(R.string.general_powered)
        applyThemeAttributes()
        applyRemoteThemeConfig(Dependencies.gliaThemeManager.theme)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setController(Dependencies.controllerFactory.visitorCodeController)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        controller?.onDestroy() ?: destroyTimer()
    }

    override fun notifySetupComplete() {
        controller?.onLoadVisitorCode()
    }

    internal fun setClosable(isClosable: Boolean) {
        closeButton.isVisible = isClosable
    }

    override fun setTimer(duration: Long) {
        Logger.d(TAG, "Setting visitor code timeout to $duration")
        timer = object : CountDownTimer(duration, duration) {
            override fun onTick(p0: Long) {
                // no-op
            }

            override fun onFinish() {
                Logger.d(TAG, "Reloading Visitor Code")
                controller?.onLoadVisitorCode()
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
        this.controller?.setView(this, closeButton.isVisible)
    }

    override fun startLoading() {
        runOnUi {
            showProgressBar(true)
            showSuccess()
            successTitle.setLocaleContentDescription(R.string.android_visitor_code_loading)
            successTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
            closeButton.setLocaleContentDescription(R.string.call_visualizer_visitor_code_close_accessibility_hint)
        }
    }

    override fun showError(throwable: Throwable) {
        throwable.message?.let { Logger.e(TAG, it, throwable) }
        runOnUi {
            showProgressBar(false)
            showFailure()
        }
    }

    private fun showSuccess() {
        successTitle.setLocaleText(R.string.call_visualizer_visitor_code_title)
        successContainer.visibility = VISIBLE
        failureContainer.visibility = GONE
    }

    private fun showFailure() {
        failureTitle.setLocaleText(R.string.visitor_code_failed)
        refreshButton.setLocaleContentDescription(R.string.call_visualizer_visitor_code_refresh_accessibility_hint)
        refreshButton.setLocaleText(R.string.general_refresh)
        failureContainer.visibility = VISIBLE
        successContainer.visibility = GONE
    }

    override fun showVisitorCode(visitorCode: VisitorCode) {
        runOnUi {
            showProgressBar(false)
            successTitle.contentDescription = localeProvider.getString(R.string.call_visualizer_visitor_code_title_accessibility_hint)
                .combineStringWith(visitorCode.code.separateStringWithSymbol("-"), " ")
            successTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
            charCodeView.setText(visitorCode.code)
        }
    }

    private fun showProgressBar(show: Boolean) {
        if (show) {
            charCodeView.alpha = 0f
            progressBar.visibility = VISIBLE
        } else {
            progressBar.visibility = GONE
            charCodeView.animate().apply {
                alpha(1f)
                duration = 200
                start()
            }
        }
    }

    private fun runOnUi(function: () -> Unit) {
        uiThreadExecutor?.execute(function) ?: post(function)
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
        logoContainer.applyWhiteLabel(theme?.isWhiteLabel)
    }

    /**
     * The values `visitor_code_view.xml` cannot express on its own: the typeface (a `textAppearance`
     * would win over it), the refresh button's background (`Application.Glia.Chat.Button.Positive`
     * sets only the `android:`-prefixed `backgroundTint`, which `MaterialButton` ignores), the
     * white-label flag, the Glia logo's tint (`app:tint` needs AppCompat view inflation, which an
     * integrator-hosted `ImageView` cannot be relied on to get), and this view's own background -
     * the layout root is a child of it.
     */
    private fun applyThemeAttributes() {
        context.applyGliaThemeFont(successTitle, failureTitle, refreshButton)
        refreshButton.backgroundTintList = ColorStateList.valueOf(
            context.gliaAttrColor(R.attr.gliaBrandPrimaryColor, R.color.glia_primary_color)
        )
        logoContainer.isVisible = !context.gliaAttrBoolean(R.attr.whiteLabel)
        logoView.applyImageColorTheme(context.gliaAttrColor(R.attr.gliaBaseShadeColor, R.color.glia_shade_color))

        setBackgroundColor(context.gliaAttrColor(R.attr.gliaBaseLightColor, R.color.glia_light_color))
    }
}
