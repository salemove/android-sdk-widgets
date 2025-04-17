package com.glia.widgets

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RawRes
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.InstantAnimationsRule
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.glia.widgets.snapshotutils.OnTestEnded
import com.glia.widgets.snapshotutils.SnapshotContent
import com.glia.widgets.snapshotutils.SnapshotProviderImp
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotStrings
import com.glia.widgets.snapshotutils.SnapshotTestLifecycle
import com.glia.widgets.snapshotutils.SnapshotTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.BufferedReader

@Suppress("MemberVisibilityCanBePrivate")
internal open class SnapshotTest(
    val deviceConfig: DeviceConfig = DeviceConfig.PIXEL_4A,
    val renderingMode: RenderingMode = RenderingMode.SHRINK,
    val showSystemUi: Boolean = false,
    val theme: String = "ThemeOverlay_Glia_Chat_Material",
    val maxPercentDifference: Double = 0.001,
    @get:Rule var animationsRule: TestRule = InstantAnimationsRule()
) : SnapshotTestLifecycle, SnapshotContent, SnapshotStrings, SnapshotTheme, SnapshotProviders {

    @Suppress("PropertyName")
    @get:Rule
    val _paparazzi = Paparazzi(
        deviceConfig = deviceConfig,
        renderingMode = renderingMode,
        showSystemUi = showSystemUi,
        theme = theme,
        maxPercentDifference = maxPercentDifference
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

    override var _snapshotProvider:SnapshotProviderImp = SnapshotProviderImp(mock<Context>())

    fun snapshot(
        view: View,
        name: String? = null,
        offsetMillis: Long = 0L,
        deviceConfig: DeviceConfig? = null,
        theme: String? = null,
        renderingMode: RenderingMode? = null
    ) {
        if (deviceConfig != null || theme != null || renderingMode != null) {
            _paparazzi.unsafeUpdateConfig(deviceConfig, theme, renderingMode)
        }
        _paparazzi.snapshot(view, name, offsetMillis)
    }

    @Before
    open fun setUp() {
        providerMockReset()
    }

    @After
    open fun tearDown() {
        _paparazzi.close()
        onEndListeners.forEach { it() }
        onEndListeners.clear()
    }

    private val onEndListeners: MutableList<OnTestEnded> = mutableListOf()
    override fun setOnEndListener(listener: OnTestEnded) {
        onEndListeners.add(listener)
    }

    companion object {
        /**
         * Useful for the view that needed to be tested on full-width size.
         * It keeps the height compact compared to other [RenderingMode] modes.
         */
        val fullWidthRenderMode = mock<RenderingMode>().also {
            whenever(it.horizAction).thenReturn(RenderingMode.SizeAction.KEEP)
            whenever(it.vertAction).thenReturn(RenderingMode.SizeAction.SHRINK)
        }

        /**
         * It can be used as an [SnapshotTest.animationsRule] or other rules to disable them.
         */
        val dummyRule = TestRule { base, _ -> base }
    }
}
