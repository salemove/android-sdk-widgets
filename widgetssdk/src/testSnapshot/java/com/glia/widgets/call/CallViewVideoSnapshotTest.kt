package com.glia.widgets.call

import com.android.ide.common.rendering.api.SessionParams
import com.glia.androidsdk.Engagement
import com.glia.widgets.SnapshotTest
import org.junit.Test

internal class CallViewVideoSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotCallView {

    // MARK: Init call

    private fun initCallState() = callState()
        .initCall(requestedMediaType = Engagement.MediaType.VIDEO)

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
        .operatorConnected()

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

    // MARK: 1-way video call started

    private fun video1WayCallStartedState(): CallState = operatorConnectedState()
        .engagementStarted()
        .visitorMediaStateChanged(visitorMediaState(video = null))
        .videoCallOperatorVideoStarted()
        .changeNumberOfMessages(1)
        .speakerValueChanged(true)

    @Test
    fun video1WayCallStarted() {
        snapshot(
            setupView(
                callState = video1WayCallStartedState()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithUiTheme() {
        snapshot(
            setupView(
                callState = video1WayCallStartedState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithGlobalColors() {
        snapshot(
            setupView(
                callState = video1WayCallStartedState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = video1WayCallStartedState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = video1WayCallStartedState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }

    // MARK: 2-way video call started

    private fun video2WayCallStartedState(): CallState = operatorConnectedState()
        .engagementStarted()
        .visitorMediaStateChanged()
        .videoCallOperatorVideoStarted()
        .changeNumberOfMessages(1)
        .speakerValueChanged(true)

    @Test
    fun video2WayCallStarted() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hideOnHold()
                }
            ).root
        )
    }

    @Test
    fun video2WayCallStartedWithUiTheme() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hideOnHold()
                },
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun video2WayCallStartedWithGlobalColors() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hideOnHold()
                },
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun video2WayCallStartedWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hideOnHold()
                },
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun video2WayCallStartedWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hideOnHold()
                },
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }

    // MARK: On Hold

    private fun onHoldState(): CallState = video2WayCallStartedState()
        .setOnHold(true)

    @Test
    fun onHold() {
        snapshot(
            setupView(
                callState = onHoldState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hide()
                }
            ).root
        )
    }

    @Test
    fun onHoldWithUiTheme() {
        snapshot(
            setupView(
                callState = onHoldState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hide()
                },
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun onHoldWithGlobalColors() {
        snapshot(
            setupView(
                callState = onHoldState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hide()
                },
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun onHoldWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = onHoldState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hide()
                },
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun onHoldWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = onHoldState(),
                floatingVisitorVideoViewCallback = { view, state ->
                    view.show(state!!.callStatus.visitorMediaState)
                    view.hide()
                },
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }
}
