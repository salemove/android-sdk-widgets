package com.glia.widgets.call

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import org.junit.Test

/**
 * The call screen rendered against `Test.Glia.Customized`, the stand-in for an integrator's
 * `GliaTheme`.
 *
 * The app bar is the point. Phase 7 replaced `CallView.setAppBarTheme()`'s hard-coded palette with
 * `ThemeOverlay.Glia.Internal.CallAppBar`, applied as a view-level `android:theme` in
 * `call_view.xml`, so the bar must stay dark-with-white-content over the video surface *while the
 * rest of the screen follows the customization*. A pre-baked customized theme is the only way to see
 * that split - the default suites render both halves the same colour, so they cannot tell a working
 * overlay from a missing one.
 */
internal class CallViewCustomizedThemeSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL,
    theme = "Test_Glia_Customized"
), SnapshotCallView {

    @Test
    fun initialState() {
        snapshot(
            setupView(
                callState = callState()
            ).root
        )
    }

    @Test
    fun initCall() {
        snapshot(
            setupView(
                callState = callState().initCall()
            ).root
        )
    }

    @Test
    fun audioCallStarted() {
        snapshot(
            setupView(
                callState = callState()
                    .initCall()
                    .operatorConnected()
                    .engagementStarted()
                    .visitorMediaStateChanged(visitorMediaState(video = null))
                    .audioCallStarted()
                    .changeNumberOfMessages(1)
                    .speakerValueChanged(true)
            ).root
        )
    }
}
