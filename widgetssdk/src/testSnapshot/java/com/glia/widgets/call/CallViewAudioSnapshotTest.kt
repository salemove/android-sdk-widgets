package com.glia.widgets.call

import com.glia.widgets.SnapshotTest
import org.junit.Test

internal class CallViewAudioSnapshotTest : SnapshotTest(), SnapshotCallView {

    override val callViewMock = SnapshotCallView.Mock(this)

    override fun setUp() {
        super.setUp()
        callViewMock.setUp()
    }

    override fun tearDown() {
        super.tearDown()
        callViewMock.tearDown()
    }

    // MARK: Init call

    private fun initCallState() = callState().initCall()

    @Test
    fun initCall() {
        snapshotFullSize(
            setupView(
                callState = initCallState()
            ).root
        )
    }

    @Test
    fun initCallWithUiTheme() {
        snapshotFullSize(
            setupView(
                callState = initCallState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun initCallWithGlobalColors() {
        snapshotFullSize(
            setupView(
                callState = initCallState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun initCallWithUnifiedTheme() {
        snapshotFullSize(
            setupView(
                callState = initCallState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun initCallWithUnifiedThemeWithoutCall() {
        snapshotFullSize(
            setupView(
                callState = initCallState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }

    // MARK: Operator connected

    private fun operatorConnectedState(): CallState = initCallState()
        .operatorConnected()

    @Test
    fun operatorConnected() {
        snapshotFullSize(
            setupView(
                callState = operatorConnectedState()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithUiTheme() {
        snapshotFullSize(
            setupView(
                callState = operatorConnectedState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithGlobalColors() {
        snapshotFullSize(
            setupView(
                callState = operatorConnectedState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithUnifiedTheme() {
        snapshotFullSize(
            setupView(
                callState = operatorConnectedState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun operatorConnectedWithUnifiedThemeWithoutCall() {
        snapshotFullSize(
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
        snapshotFullSize(
            setupView(
                callState = audioCallStartedState()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithUiTheme() {
        snapshotFullSize(
            setupView(
                callState = audioCallStartedState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithGlobalColors() {
        snapshotFullSize(
            setupView(
                callState = audioCallStartedState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithUnifiedTheme() {
        snapshotFullSize(
            setupView(
                callState = audioCallStartedState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun audioCallStartedWithUnifiedThemeWithoutCall() {
        snapshotFullSize(
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
        snapshotFullSize(
            setupView(
                callState = onHoldState()
            ).root
        )
    }

    @Test
    fun onHoldWithUiTheme() {
        snapshotFullSize(
            setupView(
                callState = onHoldState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun onHoldWithGlobalColors() {
        snapshotFullSize(
            setupView(
                callState = onHoldState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun onHoldWithUnifiedTheme() {
        snapshotFullSize(
            setupView(
                callState = onHoldState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun onHoldWithUnifiedThemeWithoutCall() {
        snapshotFullSize(
            setupView(
                callState = onHoldState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }
}
