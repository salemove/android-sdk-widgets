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
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.unifiedui.exstensions.*
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
    private var controller: EndScreenSharingContract.Controller? = null
    private var uiTheme: UiTheme by Delegates.notNull()
    private var defaultStatusBarColor: Int by Delegates.notNull()
    private var statusBarColor: Int by Delegates.notNull()
    private var screenSharingController: ScreenSharingController? = null

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
            defaultStatusBarColor = Utils.getActivity(context).window.statusBarColor
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
        applyStyle()
    }

    private fun applyStyle() {
        val theme = Dependencies.getGliaThemeManager().theme?.callVisualizerTheme?.endScreenSharingTheme
        binding.appBarView.applyHeaderTheme(theme?.header)
        binding.endSharingButton.applyButtonTheme(theme?.endButton)
        binding.screenSharingLabel.applyTextTheme(theme?.label)
        binding.root.applyLayerTheme(theme?.background)
    }

    fun onDestroy() {
        changeStatusBarColor(defaultStatusBarColor)
    }


    interface OnFinishListener {
        fun finish()
    }
}
