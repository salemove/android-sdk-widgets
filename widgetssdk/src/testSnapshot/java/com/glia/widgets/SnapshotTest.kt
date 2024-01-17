package com.glia.widgets

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.RawRes
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.snapshotutils.SnapshotContent
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.snapshotutils.SnapshotTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.io.BufferedReader

open class SnapshotTest : SnapshotContent, SnapshotStrings, SnapshotTheme {
    private val deviceConfig = DeviceConfig.PIXEL_4A

    @Suppress("PropertyName")
    @get:Rule
    val _paparazzi = Paparazzi(
        deviceConfig = deviceConfig,
        renderingMode = SessionParams.RenderingMode.SHRINK,
        showSystemUi = false,
        theme = "ThemeOverlay_Glia_Chat_Material"
    )

    override val context: Context
        get() = _paparazzi.context

    override val resources: Resources
        get() = _paparazzi.resources

    override val layoutInflater: LayoutInflater
        get() = _paparazzi.layoutInflater

    override fun rawRes(@RawRes resId: Int): String {
        return resources.openRawResource(resId).use {
            BufferedReader(it.reader()).readText()
        }
    }

    fun snapshot(view: View, name: String? = null, offsetMillis: Long = 0L) {
        _paparazzi.snapshot(view, name, offsetMillis)
    }

    /**
     * This function is intended for views that might collapse due to [SessionParams.RenderingMode.SHRINK] mode.
     * It will still keep snapshots compact compared to other modes.
     */
    fun snapshotFullWidth(view: View, name: String? = null, offsetMillis: Long = 0L) {
        val viewWrapper = FrameLayout(context).apply { addView(view, LayoutParams(deviceConfig.screenWidth, LayoutParams.WRAP_CONTENT)) }
        _paparazzi.snapshot(viewWrapper, name, offsetMillis)
    }

    /**
     * This function is intended for views that might collapse due to [SessionParams.RenderingMode.SHRINK] mode.
     * It will still keep snapshots compact compared to other modes.
     */
    fun snapshotFullSize(
        view: View,
        name: String? = null,
        offsetMillis: Long = 0L,
        width: Int = deviceConfig.screenWidth,
        height: Int = deviceConfig.screenHeight
    ) {
        val viewWrapper = FrameLayout(context).apply { addView(view, LayoutParams(width, height)) }
        snapshot(viewWrapper, name, offsetMillis)
    }

    @Before
    open fun setUp() {
    }

    @After
    open fun tearDown() {
    }
}
