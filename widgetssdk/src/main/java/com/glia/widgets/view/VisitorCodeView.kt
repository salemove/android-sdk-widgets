package com.glia.widgets.view

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.CountDownTimer
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
import com.glia.widgets.core.callvisualizer.domain.CallVisualizer
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.unifiedui.exstensions.applyButtonTheme
import com.glia.widgets.view.unifiedui.exstensions.applyLayerTheme
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.exstensions.layoutInflater
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.glia.widgets.view.unifiedui.exstensions.applyImageColorTheme

/**
 * A view for displaying the visitor code to the visitor.
 *
 * This view should not be instantiated directly - neither programmatically nor inside your XML layouts.
 * Use [CallVisualizer.createVisitorCodeView] to create an instance of this view.
 */
class VisitorCodeView internal constructor(
    context: Context
) : FrameLayout(MaterialThemeOverlay.wrap(context, null, 0, R.style.Application_Glia_Chat), null, 0), VisitorCodeContract.View {
    private val TAG = VisitorCodeView::class.java.simpleName
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

    init {
        layoutInflater.inflate(R.layout.visitor_code_view, this, true)
        successContainer = findViewById(R.id.success_container)
        failureContainer = findViewById(R.id.failure_container)
        successTitle = findViewById(R.id.title_view)
        failureTitle = findViewById(R.id.failure_title)
        charCodeView = findViewById(R.id.codeView)
        progressBar = findViewById(R.id.progress_bar)
        refreshButton = findViewById(R.id.failure_refresh_button)
        refreshButton.setOnClickListener { controller.onLoadVisitorCode() }
        closeButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener { controller.onCloseButtonClicked() }
        readTypedArray()
        applyRemoteConfigTheme()
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
        timer = object : CountDownTimer(duration, duration) {
            override fun onTick(p0: Long) {
                //no-op
            }

            override fun onFinish() {
                controller.onLoadVisitorCode()
            }
        }.start()
    }

    override fun cleanUpOnDestroy() {
        timer?.cancel()
        timer = null
    }

    override fun setController(controller: VisitorCodeContract.Controller) {
        this.controller = controller
        this.controller.setView(this)
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
        applyDialogTheme(theme)
    }

    override fun startLoading() {
        runOnUi {
            showProgressBar(true)
            showSuccess()
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
            successTitle.applyTextTheme(this?.visitorCodeTheme?.title)
            // TODO MOB-1827
            failureTitle.applyTextTheme(this?.visitorCodeTheme?.title)
            progressBar.indeterminateTintList = this?.visitorCodeTheme?.loadingProgressBar?.primaryColorStateList
            closeButton.applyImageColorTheme(this?.visitorCodeTheme?.closeButtonColor)
            // TODO MOB-1827
            refreshButton.applyButtonTheme(this?.endScreenSharingTheme?.endButton)
        }
    }

    private fun applyDialogTheme(theme: UiTheme?) {
        if (theme == null) {
            Logger.d(TAG, "UiTheme is null!")
            return
        }

        theme.baseDarkColor?.let {
            ContextCompat.getColor(context, it).apply {
                successTitle.setTextColor(this)
                failureTitle.setTextColor(this)
            }
        }
        theme.fontRes?.let {
            ResourcesCompat.getFont(context, it)?.run {
                successTitle.typeface = this
                failureTitle.typeface = this
            }
        }
        theme.brandPrimaryColor?.let {
            ContextCompat.getColor(context, it).apply {
                refreshButton.backgroundTintList = ColorStateList.valueOf(this)
            }
        }
        theme.baseLightColor?.run {
            rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.glia_base_light_color))
        }
    }
}
