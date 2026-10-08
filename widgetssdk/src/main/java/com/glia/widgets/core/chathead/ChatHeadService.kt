package com.glia.widgets.core.chathead

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Size
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.graphics.toPointF
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.applyGliaThemeOverlays
import com.glia.widgets.internal.chathead.BubblePosition
import com.glia.widgets.internal.chathead.BubbleRenderTarget
import com.glia.widgets.internal.chathead.BubbleUiModel
import com.glia.widgets.internal.chathead.ChatBubbleContract
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.view.SimpleTouchListener
import com.glia.widgets.view.head.ChatHeadLogger
import com.glia.widgets.view.head.ChatHeadView
import com.glia.widgets.view.head.ChatHeadView.Companion.getInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This service is used to display the Glia chat head (chat bubble) outside of the integrator's app.
 */
internal class ChatHeadService : Service() {
    private val windowManager: WindowManager by lazy { getSystemService()!! }

    private val chatHeadSize: Int get() = resources.getDimensionPixelSize(R.dimen.glia_chat_head_size)

    private val chatHeadMargin: Int get() = resources.getDimensionPixelSize(R.dimen.glia_chat_head_content_padding)

    private val layoutFlag: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    private val displaySize: Size get() = obtainScreenSize()

    private fun createLayoutParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

    private var chatHeadView: ChatHeadView? = null

    private var controller: ChatBubbleContract.Controller? = null

    /** The single instance handed to `WindowManager`; dragging mutates it in place. */
    private var windowParams: WindowManager.LayoutParams? = null

    private var isWindowAdded: Boolean = false

    private var scope: CoroutineScope? = null

    private fun obtainScreenSize(): Size {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
            val width = metrics.bounds.width() - insets.left - insets.right
            val height = metrics.bounds.height() - insets.bottom - insets.top
            return Size(width, height)
        }
        return DisplayMetrics().also(windowManager.defaultDisplay::getMetrics)
            .run { Size(widthPixels, heightPixels) }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun attachBaseContext(newBase: Context) {
        /*Since a Service doesn't really have a theme, need to force style to avoid crashes in views attached with this context.
        Otherwise, this leads to exceptions like "You need to use a Theme.AppCompat theme (or descendant) with ShapeableImageView.*/

        /* The wrapper must NOT be asked for its theme yet. Service.attach() calls attachBaseContext()
           before assigning mBase, and materialising the wrapper's theme walks
           ContextImpl.getTheme() -> getOuterContext() (this Service) -> getApplicationInfo(), which
           NPEs on the still-null mBase. The Glia overlays are therefore composed in onCreate(). */
        super.attachBaseContext(ContextThemeWrapper(newBase, R.style.Theme_Glia_Internal))
    }

    override fun onCreate() {
        super.onCreate()

        // Safe here: mBase is assigned, so the base theme can be read. ContextThemeWrapper creates its
        // theme lazily and only once, so the overlays land on top of Theme.Glia.Internal and stay there.
        applyGliaThemeOverlays()

        Logger.d(TAG, "onCreate")
        val controller = Dependencies.controllerFactory.chatBubbleController
        this.controller = controller
        val params = createLayoutParams()
        windowParams = params
        initChatHeadView(controller, windowManager, params)

        // The service outlives the bubble being on screen - it is kept running for as long as the
        // overlay could be needed, because API 26+ would refuse to start it once the app is in the
        // background. So the window is added and removed here, off the render target.
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).also { scope = it }.launch {
            controller.uiState.collect(::render)
        }
    }

    private fun render(state: BubbleUiModel) {
        if (state.target != BubbleRenderTarget.SERVICE) {
            removeWindowIfPresent()
            return
        }
        addWindowIfAbsent()
        chatHeadView?.render(state)
    }

    private fun addWindowIfAbsent() {
        if (isWindowAdded) return
        val view = chatHeadView ?: return
        val params = windowParams ?: return
        // Positioned here, not at onCreate: the service runs long before the window first shows, and
        // the shared position may have moved in the meantime - the in-app bubble writes to it too.
        applyBubblePosition(params)
        Logger.i(TAG, "Bubble: add overlay window")
        windowManager.addView(view, params)
        isWindowAdded = true
    }

    /**
     * Places the window where the visitor last left the bubble — on either surface, mapped from the
     * shared fraction into this display's range — or at the default corner if it was never dragged.
     */
    private fun applyBubblePosition(params: WindowManager.LayoutParams) {
        val display = displaySize
        val position = controller?.bubblePosition
        params.x = position?.xWithin(0f, maxX(display).toFloat())?.roundToInt() ?: getDefaultXPosition(display.width)
        params.y = position?.yWithin(0f, maxY(display).toFloat())?.roundToInt() ?: getDefaultYPosition(display.height)
    }

    private fun saveBubblePosition(params: WindowManager.LayoutParams) {
        val display = displaySize
        controller?.bubblePosition = BubblePosition.within(
            x = params.x.toFloat(),
            y = params.y.toFloat(),
            xMin = 0f,
            xMax = maxX(display).toFloat(),
            yMin = 0f,
            yMax = maxY(display).toFloat()
        )
    }

    private fun maxX(display: Size): Int = display.width - chatHeadSize - chatHeadMargin

    private fun maxY(display: Size): Int = display.height - chatHeadSize - chatHeadMargin

    private fun removeWindowIfPresent() {
        if (!isWindowAdded) return
        Logger.d(TAG, "Bubble: remove overlay window")
        chatHeadView?.also(windowManager::removeView)
        isWindowAdded = false
    }

    override fun onDestroy() {
        super.onDestroy()

        Logger.d(TAG, "onDestroy")

        scope?.cancel()
        scope = null
        removeWindowIfPresent()
        chatHeadView = null
        windowParams = null
        controller = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initChatHeadView(controller: ChatBubbleContract.Controller, windowManager: WindowManager, layoutParams: WindowManager.LayoutParams) {
        chatHeadView = getInstance(this)
        chatHeadView?.setOnTouchListener(
            SimpleTouchListener(
                retrieveInitialCoordinates = { Point(layoutParams.x, layoutParams.y).toPointF() },
                onMove = { x, y ->
                    layoutParams.x = x.roundToInt()
                    layoutParams.y = y.roundToInt()
                    windowManager.updateViewLayout(chatHeadView, layoutParams)
                },
                onRelease = {
                    saveBubblePosition(layoutParams)
                    ChatHeadLogger.logPositionChanged()
                }
            ))
        chatHeadView?.setOnClickListener {
            when (controller.onBubbleTapped()) {
                Destinations.CALL_VIEW -> chatHeadView?.navigateToCall()
                Destinations.CHAT_VIEW -> chatHeadView?.navigateToChat()
            }
        }
    }

    private fun getDefaultXPosition(screenWidth: Int): Int = screenWidth - chatHeadSize - chatHeadMargin

    // 🤔Need to think why it is strictly the 4/5 of screen height
    private fun getDefaultYPosition(screenHeight: Int): Int = screenHeight / 10 * 8

    internal companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, ChatHeadService::class.java)
        }
    }
}
