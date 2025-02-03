package com.glia.widgets.view.head

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import com.glia.widgets.Constants
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatHeadLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.view.ViewHelpers
import kotlin.math.roundToInt
import kotlin.properties.Delegates

internal class ChatHeadLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.gliaChatStyle,
    defStyleRes: Int = R.style.Application_Glia_Chat
) : FrameLayout(
    context.wrapWithMaterialThemeOverlay(attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), ChatHeadLayoutContract.View {
    private var chatHeadController: ChatHeadLayoutContract.Controller by Delegates.notNull()

    private var navigationCallback: NavigationCallback? = null

    private val _chatHeadViewPosition: Pair<Int?, Int?>
        get() = Pair(chatHeadView.x.roundToInt(), chatHeadView.y.roundToInt())

    val position: Pair<Int?, Int?> get() = _chatHeadViewPosition

    private val chatHeadSize: Float by lazy { resources.getDimension(R.dimen.glia_chat_head_size) }
    private val chatHeadMargin: Float by lazy { resources.getDimension(R.dimen.glia_chat_head_content_padding) }

    private val binding: ChatHeadLayoutBinding by lazy {
        ChatHeadLayoutBinding.inflate(layoutInflater, this)
    }

    private val chatHeadView: ChatHeadView by lazy { binding.chatHeadView }

    init {
        initialize()
    }

    override fun showOperatorImage(operatorImgUrl: String) {
        chatHeadView.showOperatorImage(operatorImgUrl)
    }

    override fun showUnreadMessageCount(count: Int) {
        chatHeadView.showUnreadMessageCount(count)
    }

    override fun showPlaceholder() {
        chatHeadView.showPlaceholder()
    }

    override fun showQueueing() {
        chatHeadView.showQueueing()
    }

    override fun showScreenSharing() {
        chatHeadView.showScreenSharing()
    }

    override fun showOnHold() {
        chatHeadView.showOnHold()
    }

    override fun hideOnHold() {
        chatHeadView.hideOnHold()
    }

    override fun navigateToChat() {
        if (navigationCallback != null) {
            navigationCallback!!.onNavigateToChat()
        } else {
            chatHeadView.navigateToChat()
        }
    }

    override fun navigateToCall() {
        if (navigationCallback != null) {
            navigationCallback!!.onNavigateToCall()
        } else {
            chatHeadView.navigateToCall()
        }
    }

    override fun navigateToEndScreenSharing() {
        if (navigationCallback != null) {
            navigationCallback!!.onNavigateToEndScreenSharing()
        } else {
            chatHeadView.navigateToEndScreenSharing()
        }
    }

    override fun show() {
        post { visibility = VISIBLE }
    }

    override fun hide() {
        post { visibility = GONE }
    }

    override fun setController(controller: ChatHeadLayoutContract.Controller) {
        chatHeadController = controller
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val isPositioned = chatHeadView.x != 0f && chatHeadView.y != 0f
        if (isPositioned) return

        val floatingViewX = w - chatHeadSize - chatHeadMargin
        val floatingViewY = h / 10f * 8f
        chatHeadView.x = floatingViewX
        chatHeadView.y = floatingViewY
        chatHeadView.invalidate()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDetachedFromWindow() {
        chatHeadController.onDestroy()
        super.onDetachedFromWindow()
    }

    /**
     * Method that allows integrator to override navigation on click with using own paths
     *
     *
     * if set to null default navigation is restored
     *
     * @param callback
     */
    fun setNavigationCallback(callback: NavigationCallback) {
        navigationCallback = callback
    }

    private fun initialize() {
        visibility = GONE
        initConfigurations()
        setupViewActions()
        setController(Dependencies.controllerFactory.chatHeadLayoutController)
        chatHeadController.setView(this)
    }

    private fun initConfigurations() {
        isClickable = false
        isFocusable = false
        ViewCompat.setElevation(this, Constants.WIDGETS_SDK_LAYER_ELEVATION)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViewActions() {
        chatHeadView.setOnTouchListener(
            ViewHelpers.OnTouchListener(
                { _chatHeadViewPosition },
                ::onChatHeadDragged
            ) { onChatHeadClicked() }
        )
    }

    private fun onChatHeadDragged(x: Float, y: Float) {
        chatHeadView.x = x
        chatHeadView.y = y
        chatHeadView.invalidate()
    }

    private fun onChatHeadClicked() = chatHeadController.onChatHeadClicked()

    fun setPosition(x: Float, y: Float) {
        chatHeadView.x = x
        chatHeadView.y = y
        chatHeadView.invalidate()
    }

    fun removeSelf() {
        (parent as? ViewGroup)?.removeView(this)
    }

    interface NavigationCallback {
        fun onNavigateToChat()
        fun onNavigateToCall()
        fun onNavigateToEndScreenSharing()
    }
}
