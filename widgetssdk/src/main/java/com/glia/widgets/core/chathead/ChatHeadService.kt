package com.glia.widgets.core.chathead

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Size
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.util.Pair
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.view.ViewHelpers
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.head.ChatHeadPosition
import com.glia.widgets.view.head.ChatHeadView
import com.glia.widgets.view.head.ChatHeadView.Companion.getInstance
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

    private val layoutParams: WindowManager.LayoutParams
        get() = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

    private var chatHeadView: ChatHeadView? = null

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
        super.attachBaseContext(ContextThemeWrapper(newBase, R.style.Application_Glia_Chat_Activity))
    }

    override fun onCreate() {
        super.onCreate()

        Logger.d(TAG, "onCreate")
        val controller = Dependencies.controllerFactory.chatHeadController
        val layoutParams = layoutParams
        initChatHeadPosition(layoutParams, controller.chatHeadPosition)
        initChatHeadView(controller, windowManager, layoutParams)
        controller.onSetChatHeadView(chatHeadView!!)
        controller.updateChatHeadView()
        windowManager.addView(chatHeadView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()

        chatHeadView?.also(windowManager::removeView)
        Logger.d(TAG, "onDestroy")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initChatHeadView(
        controller: ChatHeadContract.Controller,
        windowManager: WindowManager,
        layoutParams: WindowManager.LayoutParams
    ) {
        chatHeadView = getInstance(this)
        chatHeadView!!.setOnTouchListener(
            ViewHelpers.OnTouchListener(
                { Pair(layoutParams.x, layoutParams.y) },
                { x: Float, y: Float ->
                    layoutParams.x = x.roundToInt()
                    layoutParams.y = y.roundToInt()
                    windowManager.updateViewLayout(chatHeadView, layoutParams)
                    controller.onChatHeadPositionChanged(layoutParams.x, layoutParams.y)
                }
            ) { controller.onChatHeadClicked() }
        )
    }

    private fun initChatHeadPosition(
        params: WindowManager.LayoutParams,
        chatHeadPosition: ChatHeadPosition
    ) {
        val display = displaySize
        params.x = chatHeadPosition.posX ?: getDefaultXPosition(display.width)
        params.y = chatHeadPosition.posY ?: getDefaultYPosition(display.height)
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
