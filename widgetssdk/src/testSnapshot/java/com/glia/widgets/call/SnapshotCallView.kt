package com.glia.widgets.call

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.OperatorMediaState
import com.glia.androidsdk.comms.Video
import com.glia.androidsdk.comms.VideoView
import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.widgets.R
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.databinding.CallActivityBinding
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotActivityWindow
import com.glia.widgets.snapshotutils.SnapshotContent
import com.glia.widgets.snapshotutils.SnapshotLottie
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotSchedulers
import com.glia.widgets.snapshotutils.SnapshotTheme
import com.glia.widgets.snapshotutils.SnapshotThemeConfiguration
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract.FlipButtonState
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

// TODO: move to com.glia.widgets.snapshotutils after CallState refactored
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal interface SnapshotCallView : SnapshotContent, SnapshotTheme, SnapshotActivityWindow,
    SnapshotProviders, SnapshotSchedulers, SnapshotLottie, SnapshotThemeConfiguration {

    data class Mock(
        val activityMock: SnapshotActivityWindow.Mock,
        val schedulersMock: SnapshotSchedulers.Mock,
        val controllerFactoryMock: ControllerFactory,
        val callControllerMock: CallContract.Controller
    )

    fun callViewMock(): Mock {
        val activityMock = activityWindowMock()
        val schedulersMock = schedulersMock()

        lottieMock()

        val controllerFactoryMock = mock<ControllerFactory>()
        val callControllerMock = mock<CallContract.Controller>()
        whenever(controllerFactoryMock.callController).thenReturn(callControllerMock)
        Dependencies.controllerFactory = controllerFactoryMock

        return Mock(activityMock, schedulersMock, controllerFactoryMock, callControllerMock)
    }

    data class ViewData(
        val root: View,
        val callView: CallView,
        val mock: Mock
    )

    fun setupView(
        callState: CallState? = null,
        executor: Executor? = Executor(Runnable::run),
        unifiedTheme: UnifiedTheme? = null,
        callViewCallback: ((CallContract.View, callState: CallState?) -> Unit)? = null
    ): ViewData {
        setUnifiedTheme(unifiedTheme)
        val mock = callViewMock()

        val callViewCaptor: KArgumentCaptor<CallContract.View> = argumentCaptor()

        val callActivityBinding = CallActivityBinding.inflate(layoutInflater)
        val root = callActivityBinding.root
        val callView = callActivityBinding.callView
        verify(mock.callControllerMock).setView(callViewCaptor.capture())

        callView.executor = executor

        val callViewContract = callViewCaptor.lastValue
        callState?.let { callViewContract.emitState(it) }
        callViewCallback?.invoke(callViewContract, callState)
        return ViewData(root, callView, mock)
    }

    fun callState(
        isCallVisualizer: Boolean = false
    ): CallState = CallState.Builder()
        .setIntegratorCallStarted(false)
        .setVisible(false)
        .setMessagesNotSeen(0)
        .setCallStatus(CallStatus.EngagementNotOngoing(null))
        .setLandscapeLayoutControlsVisible(false)
        .setIsSpeakerOn(false)
        .setIsMuted(false)
        .setHasVideo(false)
        .setIsCallVisualizer(isCallVisualizer)
        .createCallState()

    fun unifiedThemeWithoutCall(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.remove("callScreen")
    }

    fun unifiedThemeWithoutFlipButton(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "callScreen",
            (unifiedTheme.remove("callScreen") as JsonObject).also { callScreen ->
                callScreen.add(
                    "visitorVideo",
                    (callScreen.remove("visitorVideo") as JsonObject).also {
                        it.remove("flipCameraButton")
                    }
                )
            }
        )
    }

    fun visitorMediaState(
        audio: Audio? = audio(),
        video: Video? = video(title = "User Video")
    ) = mock<VisitorMediaState>().also {
        whenever(it.audio).thenReturn(audio)
        whenever(it.video).thenReturn(video)
    }

    fun operatorMediaState(
        audio: Audio? = audio(),
        video: Video? = video(title = "Operator Video")
    ) = mock<OperatorMediaState>().also {
        whenever(it.audio).thenReturn(audio)
        whenever(it.video).thenReturn(video)
    }

    fun audio(
        status: Media.Status = Media.Status.PLAYING
    ) = mock<Audio>().also {
        whenever(it.status).thenReturn(status)
    }

    fun video(
        status: Media.Status = Media.Status.PLAYING,
        title: String,
        backgroundColor: Int = Color.BLACK,
        textColor: Int = Color.WHITE
    ) = mock<Video>().also {
        whenever(it.status).thenReturn(status)
        val videoView = videoView(title, backgroundColor, textColor)
        whenever(it.createVideoView(any())).thenReturn(videoView)
    }

    fun videoView(
        title: String,
        backgroundColor: Int = Color.BLACK,
        textColor: Int = Color.WHITE
    ) = object : VideoView(context) {
        init {
            addView(
                TextView(context).apply {
                    text = title
                    setBackgroundColor(backgroundColor)
                    setTextColor(textColor)
                    gravity = Gravity.CENTER
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                }
            )
        }

        override fun pauseRendering() {}

        override fun resumeRendering() {}

        override fun setZOrderMediaOverlay(isMediaOverlay: Boolean) {}

        override fun release() {}

        override fun setGravity(gravity: Int) {}

        override fun setScalingType(scalingType: ScalingType?) {}
    }

    fun CallState.initCall(
        companyName: String = "Snapshot tests",
        requestedMediaType: MediaType = MediaType.AUDIO,
        visitorMediaState: VisitorMediaState? = null,
        showFlipVisitorCameraButton: Boolean = false,
        isOnHold: Boolean = false
    ): CallState = this
        .initCall(requestedMediaType)
        .visitorMediaStateChanged(visitorMediaState)
        .setOnHold(isOnHold)
        .landscapeControlsVisibleChanged(false)

    fun CallState.operatorConnected(
        name: String = "Screenshot Operator",
        url: String? = null,
        operatorMediaState: OperatorMediaState = operatorMediaState(),
        formattedTime: String = "00:01"
    ): CallState = this
        .operatorConnecting(name, url)
        //To replicate a real situation, need to pass here not-empty operatorMediaState
        .videoCallOperatorVideoStarted(operatorMediaState, formattedTime)

    fun CallState.audioCallStarted(
        operatorMediaState: OperatorMediaState = operatorMediaState(video = null),
        formattedTime: String = "00:42"
    ): CallState = this.audioCallStarted(operatorMediaState, formattedTime)

    fun CallState.videoCallOperatorVideoStarted(
        operatorMediaState: OperatorMediaState = operatorMediaState(),
        formattedTime: String = "00:42"
    ): CallState = this.videoCallOperatorVideoStarted(operatorMediaState, formattedTime)

    fun CallState.visitorMediaStateChanged(
        visitorMediaState: VisitorMediaState = visitorMediaState()
    ): CallState = this.visitorMediaStateChanged(visitorMediaState)

    fun CallState.flipButtonStateChanged(
        flipButtonState: FlipButtonState = FlipButtonState.SWITCH_TO_BACK_CAMERA
    ): CallState = this.flipButtonStateChanged(flipButtonState)
}
