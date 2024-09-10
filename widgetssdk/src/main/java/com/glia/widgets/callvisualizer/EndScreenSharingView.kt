package com.glia.widgets.callvisualizer

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.EndScreenSharingViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.applyButtonTheme
import com.glia.widgets.helper.applyTextTheme
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.getFullHybridTheme
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay

internal class EndScreenSharingView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), EndScreenSharingContract.View {

    var onFinishListener: OnFinishListener? = null
    private var controller: EndScreenSharingContract.Controller? = null

    private val binding: EndScreenSharingViewBinding by lazy {
        EndScreenSharingViewBinding.inflate(layoutInflater, this)
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    init {
        initCallbacks()
        applyDefaultTheme(attrs, defStyleAttr, defStyleRes)
        applyRemoteTheme(Dependencies.gliaThemeManager.theme)
        setupViewTextResources()
        prepareView()
        SimpleWindowInsetsAndAnimationHandler(this, binding.appBarView)
    }

    private fun setupViewTextResources() {
        binding.appBarView.setTitle(LocaleString(R.string.call_visualizer_screen_sharing_header_title))
        binding.screenSharingLabel.setLocaleText(R.string.call_visualizer_screen_sharing_message)
        binding.endSharingButton.setLocaleText(R.string.screen_sharing_visitor_screen_end_title)
    }

    private fun applyDefaultTheme(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            var theme = Utils.getThemeFromTypedArray(this, context)
            theme.iconEndScreenShare?.let {
                binding.endSharingButton.icon = ResourcesCompat.getDrawable(context.resources, it, null)
            }
            theme = theme.getFullHybridTheme(Dependencies.sdkConfigurationManager.uiTheme)
            applyRuntimeTheme(theme)
        }
    }

    private fun prepareView() {
        binding.appBarView.hideLeaveButtons()
    }

    private fun initCallbacks() {
        binding.endSharingButton.setOnClickListener { controller?.onEndScreenSharingButtonClicked() }
        binding.appBarView.setOnBackClickedListener { controller?.onBackArrowClicked() }
    }

    override fun finish() {
        this.onFinishListener?.finish()
    }

    override fun stopScreenSharing() {
        controller?.onForceStopScreenSharing()
    }

    override fun setController(controller: EndScreenSharingContract.Controller) {
        this.controller = controller
        controller.setView(this)
    }

    private fun applyRemoteTheme(unifiedTheme: UnifiedTheme?) {
        val theme = unifiedTheme?.callVisualizerTheme?.endScreenSharingTheme ?: return
        binding.appBarView.applyHeaderTheme(theme.header)
        binding.endSharingButton.applyButtonTheme(theme.endButton)
        binding.screenSharingLabel.applyTextTheme(theme.label)
        binding.root.applyLayerTheme(theme.background)
    }

    private fun applyRuntimeTheme(theme: UiTheme?) {
        if (theme == null) {
            Logger.d(TAG, "UiTheme is null!")
            return
        }

        val systemNegativeColor = theme.systemNegativeColor?.let(::getColorCompat)
        val baseLightColor = theme.baseLightColor?.let(::getColorCompat)
        val baseDarkColor = theme.baseDarkColor?.let(::getColorCompat)
        val fontFamily = theme.fontRes?.let(::getFontCompat)

        binding.appBarView.setTheme(theme)
        binding.endSharingButton.applyButtonTheme(
            backgroundColor = systemNegativeColor,
            textColor = baseLightColor,
            textFont = fontFamily
        )
        binding.screenSharingLabel.applyTextTheme(
            textColor = baseDarkColor,
            textFont = fontFamily
        )
        binding.root.setBackgroundColor(baseLightColor ?: return)
    }

    interface OnFinishListener {
        fun finish()
    }
}
