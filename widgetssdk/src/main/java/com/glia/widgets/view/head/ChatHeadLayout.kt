package com.glia.widgets.view.head

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatHeadLayoutBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.wrapWithGliaTheme
import com.glia.widgets.internal.chathead.BubblePosition
import com.glia.widgets.internal.chathead.ChatBubbleContract
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.view.SimpleTouchListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class ChatHeadLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context.wrapWithGliaTheme(),
    attrs,
    defStyleAttr
) {
    private val chatBubbleController: ChatBubbleContract.Controller by lazy { Dependencies.controllerFactory.chatBubbleController }

    private var scope: CoroutineScope? = null

    private var navigationCallback: NavigationCallback? = null

    private val _chatHeadViewPosition: PointF
        get() = PointF(chatHeadView.x, chatHeadView.y)

    private val chatHeadSize: Float by lazy { resources.getDimension(R.dimen.glia_chat_head_size) }
    private val chatHeadBottomRightMargin: Float by lazy { resources.getDimension(R.dimen.glia_chat_head_content_padding) }
    private val chatHeadTopLeftMargin: Float by lazy { resources.getDimension(R.dimen.glia_small) }

    private val binding: ChatHeadLayoutBinding by lazy {
        ChatHeadLayoutBinding.inflate(layoutInflater, this)
    }

    private val chatHeadView: ChatHeadView by lazy { binding.chatHeadView }

    init {
        initialize()
        z = 100f // Make sure chat head is on top of other views
    }

    private fun navigateToChat() {
        navigationCallback?.onNavigateToChat() ?: chatHeadView.navigateToChat()
    }

    private fun navigateToCall() {
        navigationCallback?.onNavigateToCall() ?: chatHeadView.navigateToCall()
    }

    /**
     * Positions the bubble once the bounds are known: at the place the visitor dragged it to — shared
     * with the overlay bubble and mapped into this layout's own range, which also carries it across
     * rotations — or at the default corner if it was never dragged.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val xMax = w - chatHeadSize - chatHeadBottomRightMargin
        val yMax = h - chatHeadSize - chatHeadBottomRightMargin
        val position = chatBubbleController.bubblePosition

        chatHeadView.x = position?.xWithin(chatHeadTopLeftMargin, xMax) ?: xMax
        chatHeadView.y = position?.yWithin(chatHeadTopLeftMargin, yMax) ?: (h / 10f * 8f)
        chatHeadView.invalidate()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun saveBubblePosition() {
        chatBubbleController.bubblePosition = BubblePosition.within(
            x = chatHeadView.x,
            y = chatHeadView.y,
            xMin = chatHeadTopLeftMargin,
            xMax = width - chatHeadSize - chatHeadBottomRightMargin,
            yMin = chatHeadTopLeftMargin,
            yMax = height - chatHeadSize - chatHeadBottomRightMargin
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope?.cancel()
        scope = null
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
        initConfigurations()
        setupViewActions()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Being attached is the visibility mechanism: the watcher only attaches this layout while the
        // controller says the bubble renders in the app, and detaches it otherwise.
        visibility = VISIBLE

        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { scope = it }.launch {
            chatBubbleController.uiState.collect(chatHeadView::render)
        }

        // Request to apply insets so that we can adjust the chat head position for edge-to-edge
        ViewCompat.requestApplyInsets(this)

        // Listen for inset changes
        setupInsetsListener()
    }

    private fun setupInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            // Instead of using the `insets` parameter directly (which might have been
            // consumed by a parent), we can get the original, unaltered insets
            // for the entire window using `getRootWindowInsets`. This is more reliable
            // when you don't control the parent Activity.
            val rootInsets = ViewCompat.getRootWindowInsets(view)
            if (rootInsets == null) {
                // If we can't get root insets, return the original ones.
                return@setOnApplyWindowInsetsListener insets
            }

            // Get the insets for the system bars (status bar and navigation bar).
            val systemBarInsets = rootInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply the insets as margin to this view.
            // This will ensure that the chat head does not go beneath the system bars.
            view.updateLayoutParams<MarginLayoutParams> {
                setMargins(
                    systemBarInsets.left,
                    systemBarInsets.top,
                    systemBarInsets.right,
                    systemBarInsets.bottom
                )
            }
            // Return the original insets so that other views in the hierarchy
            // can also process them. Do not consume them here.
            insets
        }
    }

    private fun initConfigurations() {
        isClickable = false
        isFocusable = false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViewActions() {
        chatHeadView.setOnTouchListener(
            SimpleTouchListener(
                retrieveInitialCoordinates = { _chatHeadViewPosition },
                onMove = { x, y -> onChatHeadDragged(x, y) },
                onRelease = {
                    saveBubblePosition()
                    ChatHeadLogger.logPositionChanged()
                }
            )
        )
        chatHeadView.setOnClickListener { onChatHeadClicked() }
    }

    private fun onChatHeadDragged(x: Float, y: Float) {
        // Make sure the chat head doesn't go off the screen
        chatHeadView.x = x.coerceIn(chatHeadTopLeftMargin, width - chatHeadSize - chatHeadBottomRightMargin)
        // Make sure the chat head doesn't go off the screen
        chatHeadView.y = y.coerceIn(chatHeadTopLeftMargin, height - chatHeadSize - chatHeadBottomRightMargin)
        chatHeadView.invalidate()
    }

    private fun onChatHeadClicked() {
        when (chatBubbleController.onBubbleTapped()) {
            Destinations.CALL_VIEW -> navigateToCall()
            Destinations.CHAT_VIEW -> navigateToChat()
        }
    }

    fun removeSelf() {
        (parent as? ViewGroup)?.removeView(this)
    }

    interface NavigationCallback {
        fun onNavigateToChat()
        fun onNavigateToCall()
    }
}
