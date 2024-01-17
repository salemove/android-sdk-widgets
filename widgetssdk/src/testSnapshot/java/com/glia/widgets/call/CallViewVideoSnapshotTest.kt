package com.glia.widgets.call

import com.glia.androidsdk.Engagement
import com.glia.widgets.SnapshotTest
import org.junit.Test

internal class CallViewVideoSnapshotTest : SnapshotTest(), SnapshotCallView {

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

    private fun initCallState() = callState()
        .initCall(requestedMediaType = Engagement.MediaType.VIDEO)

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

    // MARK: 1-way video call started

    private fun video1WayCallStartedState(): CallState = operatorConnectedState()
        .engagementStarted()
        .visitorMediaStateChanged(visitorMediaState(video = null))
        .videoCallOperatorVideoStarted()
        .changeNumberOfMessages(1)
        .speakerValueChanged(true)

    @Test
    fun video1WayCallStarted() {
        snapshotFullSize(
            setupView(
                callState = video1WayCallStartedState()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithUiTheme() {
        snapshotFullSize(
            setupView(
                callState = video1WayCallStartedState(),
                uiTheme = uiTheme()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithGlobalColors() {
        snapshotFullSize(
            setupView(
                callState = video1WayCallStartedState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithUnifiedTheme() {
        snapshotFullSize(
            setupView(
                callState = video1WayCallStartedState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun video1WayCallStartedWithUnifiedThemeWithoutCall() {
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
        snapshotFullSize(
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
