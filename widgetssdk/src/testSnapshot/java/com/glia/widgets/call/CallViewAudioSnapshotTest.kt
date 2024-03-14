package com.glia.widgets.call

import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import org.junit.Test

internal class CallViewAudioSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotCallView {

    // MARK: Init call

    private fun initCallState() = callState().initCall()

    @Test
    fun initCall() {
        snapshot(
            setupView(
                callState = initCallState()
            ).root
        )
    }

    @Test
    fun initCallWithUiTheme() {
        snapshot(
            setupView(
                callState = initCallState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun initCallWithGlobalColors() {
        snapshot(
            setupView(
                callState = initCallState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun initCallWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = initCallState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun initCallWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = initCallState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }

    // MARK: Operator connected

    private fun operatorConnectedState(): CallState = initCallState()
        .operatorConnecting("Screenshot Operator", null)
        .videoCallOperatorVideoStarted(operatorMediaState(null, null), formattedTime = "00:01")

    @Test
    fun operatorConnected() {
        snapshot(
            setupView(
                callState = operatorConnectedState()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithUiTheme() {
        snapshot(
            setupView(
                callState = operatorConnectedState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithGlobalColors() {
        snapshot(
            setupView(
                callState = operatorConnectedState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = operatorConnectedState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = operatorConnectedState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }

    // MARK: Audio call started

    private fun audioCallStartedState(): CallState = operatorConnectedState()
        .engagementStarted()
        .visitorMediaStateChanged(visitorMediaState(video = null))
        .audioCallStarted()
        .changeNumberOfMessages(1)
        .speakerValueChanged(true)

    @Test
    fun audioCallStarted() {
        snapshot(
            setupView(
                callState = audioCallStartedState()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithUiTheme() {
        snapshot(
            setupView(
                callState = audioCallStartedState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithGlobalColors() {
        snapshot(
            setupView(
                callState = audioCallStartedState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = audioCallStartedState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = audioCallStartedState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }

    // MARK: On Hold

    private fun onHoldState(): CallState = audioCallStartedState()
        .setOnHold(true)

    @Test
    fun onHold() {
        snapshot(
            setupView(
                callState = onHoldState()
            ).root
        )
    }

    @Test
    fun onHoldWithUiTheme() {
        snapshot(
            setupView(
                callState = onHoldState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun onHoldWithGlobalColors() {
        snapshot(
            setupView(
                callState = onHoldState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun onHoldWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = onHoldState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun onHoldWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = onHoldState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }
}
