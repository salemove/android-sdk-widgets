package com.glia.widgets.call

import android.graphics.Color
import com.android.ide.common.rendering.api.SessionParams
import com.glia.widgets.SnapshotTest
import com.glia.widgets.engagement.MediaType
import org.junit.Test

internal class CallViewVideoSnapshotTest : SnapshotTest(
    renderingMode = SessionParams.RenderingMode.NORMAL
), SnapshotCallView {

    // MARK: Init call

    private fun initCallState() = callState().initCall(requestedMediaType = MediaType.VIDEO)

    @Test
    fun initCall() {
        snapshot(
            setupView(
                callState = initCallState()
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
                callState = video2WayCallStartedState()
            ).root
        )
    }

    @Test
    fun video2WayCallStartedWithGlobalColors() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun video2WayCallStartedWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun video2WayCallStartedWithUnifiedThemeWithoutCall() {
        snapshot(
            setupView(
                callState = video2WayCallStartedState(),
                unifiedTheme = unifiedThemeWithoutCall()
            ).root
        )
    }

    // MARK: 2-way video call with the flip visitor camera button

    private fun video2WayWithFlipButtonState(): CallState = video2WayCallStartedState()
        .visitorMediaStateChanged(
            visitorMediaState(video = video(title = "User camera", backgroundColor = Color.BLUE))
        )
        .videoCallOperatorVideoStarted(
            operatorMediaState = operatorMediaState(video = video(title = "Operator camera", backgroundColor = Color.GREEN))
        )
        .flipButtonStateChanged()

    @Test
    fun video2WayWithFlipButton() {
        snapshot(
            setupView(
                callState = video2WayWithFlipButtonState()
            ).root
        )
    }

    @Test
    fun video2WayWithFlipButtonWithGlobalColors() {
        snapshot(
            setupView(
                callState = video2WayWithFlipButtonState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun video2WayWithFlipButtonWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = video2WayWithFlipButtonState(),
                unifiedTheme = unifiedTheme()
            ).root
        )
    }

    @Test
    fun video2WayWithFlipButtonWithUnifiedThemeWithoutFlipButton() {
        snapshot(
            setupView(
                callState = video2WayWithFlipButtonState(),
                unifiedTheme = unifiedThemeWithoutFlipButton()
            ).root
        )
    }

    // MARK: Poor Media Quality

    private fun video2WayWithMediaQualityPoorState(): CallState = video2WayCallStartedState()
        .setIsMediaQualityPoor(true)

    @Test
    fun video2WayWithMediaQualityPoor() {
        snapshot(
            setupView(
                callState = video2WayWithMediaQualityPoorState()
            ).root
        )
    }

    @Test
    fun video2WayWithMediaQualityPoorWithGlobalColors() {
        snapshot(
            setupView(
                callState = video2WayWithMediaQualityPoorState(),
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).root
        )
    }

    @Test
    fun video2WayWithMediaQualityPoorWithUnifiedTheme() {
        snapshot(
            setupView(
                callState = video2WayWithMediaQualityPoorState(),
                unifiedTheme = unifiedTheme()
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
                callState = onHoldState()
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
