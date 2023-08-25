package com.glia.widgets

import com.glia.widgets.snapshotutils.SnapshotTheme
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RawRes
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.snapshotutils.SnapshotContent
import com.glia.widgets.snapshotutils.SnapshotStrings
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.io.BufferedReader

open class SnapshotTest : SnapshotContent, SnapshotStrings, SnapshotTheme {
    @Suppress("PropertyName")
    @get:Rule
    val _paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_4A,
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

    @Before
    open fun setUp() {}

    @After
    open fun tearDown() {}
}
