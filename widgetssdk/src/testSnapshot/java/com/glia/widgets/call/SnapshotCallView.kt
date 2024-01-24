package com.glia.widgets.call

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.OperatorMediaState
import com.glia.androidsdk.comms.Video
import com.glia.androidsdk.comms.VideoView
import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.CallActivityBinding
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.snapshotutils.SnapshotActivityWindow
import com.glia.widgets.snapshotutils.SnapshotContent
import com.glia.widgets.snapshotutils.SnapshotProviders
import com.glia.widgets.snapshotutils.SnapshotSchedulers
import com.glia.widgets.snapshotutils.SnapshotTheme
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

// TODO: move to com.glia.widgets.snapshotutils after CallState refactored
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal interface SnapshotCallView : SnapshotContent, SnapshotTheme, SnapshotActivityWindow, SnapshotProviders, SnapshotSchedulers {

    data class Mock(
        val activityMock: SnapshotActivityWindow.Mock,
        val schedulersMock: SnapshotSchedulers.Mock,
        val controllerFactoryMock: ControllerFactory,
        val callControllerMock: CallContract.Controller,
        val floatingVisitorVideoController: FloatingVisitorVideoContract.Controller
    )

    fun callViewMock(): Mock {
        val activityMock = activityWindowMock()
        val schedulersMock = schedulersMock()

        stringProviderMock()
        resourceProviderMock()

        val controllerFactoryMock = mock<ControllerFactory>()
        val callControllerMock = mock<CallContract.Controller>()
        val floatingVisitorVideoController = mock<FloatingVisitorVideoContract.Controller>()
        whenever(controllerFactoryMock.callController).thenReturn(callControllerMock)
        whenever(controllerFactoryMock.floatingVisitorVideoController).thenReturn(floatingVisitorVideoController)
        Dependencies.setControllerFactory(controllerFactoryMock)

        setOnEndListener {
            Dependencies.setControllerFactory(null)
        }

        return Mock(activityMock, schedulersMock, controllerFactoryMock, callControllerMock, floatingVisitorVideoController)
    }

    data class ViewData(
        val root: View,
        val callView: CallView,
        val mock: Mock
    )

    fun setupView(
        callState: CallState? = null,
        companyName: String? = "SnapshotCall Tests",
        executor: Executor? = Executor(Runnable::run),
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null,
        callViewCallback: ((CallContract.View, callState: CallState?) -> Unit)? = null,
        floatingVisitorVideoViewCallback: ((FloatingVisitorVideoContract.View, callState: CallState?) -> Unit)? = null
    ): ViewData {
        val mock = callViewMock()

        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }
        Dependencies.getSdkConfigurationManager().companyName = companyName

        val callViewCaptor: KArgumentCaptor<CallContract.View> = argumentCaptor()
        val floatingVisitorVideoViewCaptor: KArgumentCaptor<FloatingVisitorVideoContract.View> = argumentCaptor()

        val callActivityBinding = CallActivityBinding.inflate(layoutInflater)
        val root = callActivityBinding.root
        val callView = callActivityBinding.callView
        verify(mock.callControllerMock).setView(callViewCaptor.capture())
        verify(mock.floatingVisitorVideoController).setView(floatingVisitorVideoViewCaptor.capture())

        callView.setUiTheme(uiTheme)

        callView.executor = executor

        val callViewContract = callViewCaptor.lastValue
        callState?.let { callViewContract.emitState(it) }
        callViewCallback?.invoke(callViewContract, callState)

        floatingVisitorVideoViewCallback?.let {
            it(floatingVisitorVideoViewCaptor.lastValue, callState)
        }

        setOnEndListener {
            Dependencies.getGliaThemeManager().theme = null
            Dependencies.getSdkConfigurationManager().companyName = null
        }

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
        title: String
    ) = mock<Video>().also {
        whenever(it.status).thenReturn(status)
        whenever(it.createVideoView(any())).thenReturn(videoView(title))
    }

    fun videoView(title: String) = object:VideoView(context) {
        init {
            addView(
                TextView(context).apply {
                    text = title
                    setBackgroundColor(Color.BLACK)
                    setTextColor(Color.WHITE)
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
        queueId: String = "queueId",
        visitorContextAssetId: String = "visitorContextAssetId",
        requestedMediaType: Engagement.MediaType = Engagement.MediaType.AUDIO,
        visitorMediaState: VisitorMediaState? = null,
        isOnHold: Boolean = false
    ): CallState = this
        .initCall(companyName, queueId, visitorContextAssetId, requestedMediaType)
        .visitorMediaStateChanged(visitorMediaState)
        .setOnHold(isOnHold)
        .landscapeControlsVisibleChanged(false)

    fun CallState.operatorConnected(
        name: String = "Screenshot Operator",
        url: String? = null,
        operatorMediaState: OperatorMediaState = operatorMediaState(null, null),
        formattedTime: String = "00:01"
    ): CallState = this
        .operatorConnecting(name, url)
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
}
