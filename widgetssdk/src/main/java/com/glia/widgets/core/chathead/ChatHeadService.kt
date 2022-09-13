package com.glia.widgets.core.chathead

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.util.Pair
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.view.ViewHelpers
import com.glia.widgets.view.head.ChatHeadView
import com.glia.widgets.view.head.ChatHeadView.Companion.getInstance
import com.glia.widgets.view.head.controller.ServiceChatHeadController

class ChatHeadService : Service() {
    private val windowManager: WindowManager by lazy { getSystemService()!! }

    private val chatHeadSize: Int
        get() = resources.getDimensionPixelSize(R.dimen.glia_chat_head_size)

    private val chatHeadMargin: Int
        get() = resources.getDimensionPixelSize(R.dimen.glia_chat_head_content_padding)


    private val layoutFlag: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    private val displayMetrics: DisplayMetrics
        get() {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            return metrics
        }

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

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        Logger.d(TAG, "onCreate")
        val controller = Dependencies.getControllerFactory().chatHeadController
        val layoutParams = layoutParams
        initChatHeadPosition(layoutParams, controller.chatHeadPosition)
        initChatHeadView(controller, windowManager, layoutParams)
        controller.onSetChatHeadView(chatHeadView)
        controller.updateChatHeadView()
        windowManager.addView(chatHeadView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (chatHeadView != null) windowManager.removeView(chatHeadView)
        Logger.d(TAG, "onDestroy")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initChatHeadView(
        controller: ServiceChatHeadController,
        windowManager: WindowManager,
        layoutParams: WindowManager.LayoutParams
    ) {
        chatHeadView = getInstance(this)
        chatHeadView!!.setOnTouchListener(
            ViewHelpers.OnTouchListener(
                { Pair(layoutParams.x, layoutParams.y) },
                { x: Float, y: Float ->
                    layoutParams.x = java.lang.Float.valueOf(x).toInt()
                    layoutParams.y = java.lang.Float.valueOf(y).toInt()
                    windowManager.updateViewLayout(chatHeadView, layoutParams)
                    controller.onChatHeadPositionChanged(layoutParams.x, layoutParams.y)
                }
            ) { controller.onChatHeadClicked() })
    }

    private fun initChatHeadPosition(
        params: WindowManager.LayoutParams, chatHeadPosition: Pair<Int, Int>
    ) {
        val displayMetrics = displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        params.x = notNullOrDefault(chatHeadPosition.first, getDefaultXPosition(screenWidth))
        params.y = notNullOrDefault(chatHeadPosition.second, getDefaultYPosition(screenHeight))
    }

    private fun notNullOrDefault(item: Int?, defaultItem: Int): Int = item ?: defaultItem

    private fun getDefaultXPosition(screenWidth: Int): Int = screenWidth - chatHeadSize - chatHeadMargin

    private fun getDefaultYPosition(screenHeight: Int): Int = screenHeight / 10 * 8

    companion object {
        private val TAG = ChatHeadService::class.java.simpleName

        @JvmStatic
        fun getIntent(context: Context?): Intent {
            return Intent(context, ChatHeadService::class.java)
        }
    }
}