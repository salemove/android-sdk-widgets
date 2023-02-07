package com.glia.widgets.callvisualizer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.glia.widgets.R
import com.glia.widgets.databinding.ScreenSharingScreenViewBinding
import com.glia.widgets.view.header.AppBarView
import com.google.android.material.theme.overlay.MaterialThemeOverlay

class ScreenSharingScreenView (
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int)
    : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), ScreenSharingScreenContract.View {

    var onFinishListener: OnFinishListener? = null
    private var controller: ScreenSharingScreenContract.Controller? = null

    private val binding: ScreenSharingScreenViewBinding by lazy {
        ScreenSharingScreenViewBinding.inflate(LayoutInflater.from(this.context), this)
    }

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)


    init {
        initCallbacks()
        prepareView()
    }

    private fun prepareView() {
        binding.appBarView.hideLeaveButtons()
    }

    private fun initCallbacks() {
        binding.endScreenSharingButton.setOnClickListener { controller?.onEndScreenSharingButtonClicked() }
        binding.appBarView.setOnBackClickedListener { controller?.onBackArrowClicked() }
    }

    override fun finish() {
        this.onFinishListener?.finish()
    }

    override fun stopScreenSharing() {
        TODO("Not yet implemented")
    }

    override fun setController(controller: ScreenSharingScreenContract.Controller?) {
        this.controller = controller
        controller?.setView(this)
    }


    interface OnFinishListener {
        fun finish()
    }
}
