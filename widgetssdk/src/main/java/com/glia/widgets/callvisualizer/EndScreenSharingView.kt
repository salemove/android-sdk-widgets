package com.glia.widgets.callvisualizer

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.databinding.EndScreenSharingViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.unifiedui.extensions.*
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import kotlin.properties.Delegates

class EndScreenSharingView (
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int)
    : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), EndScreenSharingContract.View {

    var onFinishListener: OnFinishListener? = null
    private val TAG = EndScreenSharingView::class.java.simpleName
    private var controller: EndScreenSharingContract.Controller? = null
    private var uiTheme: UiTheme by Delegates.notNull()
    private var statusBarColor: Int by Delegates.notNull()
    private var screenSharingController: ScreenSharingController? = null
    private var defaultStatusBarColor: Int? = null

    private val binding: EndScreenSharingViewBinding by lazy {
        EndScreenSharingViewBinding.inflate(layoutInflater, this)
    }

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)


    init {
        initCallbacks()
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        prepareView()
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        uiTheme = Utils.getThemeFromTypedArray(typedArray, this.context)
        uiTheme.brandPrimaryColor?.let(::getColorCompat)?.also {
            statusBarColor = it
            changeStatusBarColor(it)
            defaultStatusBarColor = Utils.getActivity(context)?.window?.statusBarColor
        }
        binding.appBarView.setTheme(uiTheme)
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
        screenSharingController?.onForceStopScreenSharing()
    }

    override fun setController(controller: EndScreenSharingContract.Controller?) {
        this.controller = controller
        controller?.setView(this)
        screenSharingController = Dependencies.getControllerFactory().screenSharingController
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        applyRemoteTheme(Dependencies.getGliaThemeManager().theme)
    }

    private fun applyRemoteTheme(unifiedTheme: UnifiedTheme?) {
        val theme = unifiedTheme?.callVisualizerTheme?.endScreenSharingTheme
        binding.appBarView.applyHeaderTheme(theme?.header)
        binding.endSharingButton.applyButtonTheme(theme?.endButton)
        binding.screenSharingLabel.applyTextTheme(theme?.label)
        binding.root.applyLayerTheme(theme?.background)
    }

    private fun applyRuntimeTheme(theme: UiTheme?) {
        if (theme == null) {
            Logger.d(TAG, "UiTheme is null!")
            return
        }

        val primaryColor = theme.brandPrimaryColor?.let(::getColorCompat)
        val systemNegativeColor = theme.systemNegativeColor?.let(::getColorCompat)
        val baseLightColor = theme.baseLightColor?.let(::getColorCompat)
        val baseDarkColor = theme.baseDarkColor?.let(::getColorCompat)
        val fontFamily = theme.fontRes?.let(::getFontCompat)

        primaryColor?.also {
            statusBarColor = it
            changeStatusBarColor(it)
            defaultStatusBarColor = Utils.getActivity(context)?.window?.statusBarColor
        }
        binding.appBarView.setTheme(theme)
        binding.root.applyLayerTheme(
            backgroundColor = baseLightColor
        )
        binding.endSharingButton.applyButtonTheme(
            backgroundColor = systemNegativeColor,
            textColor = baseLightColor,
            textFont = fontFamily
        )
        binding.screenSharingLabel.applyTextTheme(
            textColor = baseDarkColor,
            textFont = fontFamily
        )
    }

    fun onDestroy() {
        changeStatusBarColor(defaultStatusBarColor)
    }


    interface OnFinishListener {
        fun finish()
    }
}
