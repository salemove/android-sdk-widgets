package com.glia.widgets.call

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.OperatorMediaState
import com.glia.androidsdk.comms.Video
import com.glia.androidsdk.comms.VideoView
import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.snapshotutils.SnapshotStringProvider
import com.glia.widgets.databinding.CallActivityBinding
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.asActivity
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.requireActivity
import com.glia.widgets.helper.rx.Schedulers
import com.glia.widgets.snapshotutils.SnapshotContent
import com.glia.widgets.snapshotutils.SnapshotTheme
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.schedulers.TestScheduler
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal interface SnapshotCallView : SnapshotContent, SnapshotTheme {
    val callViewMock: Mock

    class Mock(private val snapshotContent: SnapshotContent) {
        val computationScheduler = TestScheduler()
        val mainScheduler = TestScheduler()

        lateinit var activityMock: Activity
        lateinit var windowMock: Window

        lateinit var controllerFactoryMock: ControllerFactory
        lateinit var callControllerMock: CallController
        lateinit var floatingVisitorVideoController: FloatingVisitorVideoContract.Controller

        fun setUp(statusBarColor: Int = "#123456".toColorInt()) {
            activityMock = mock()
            windowMock = mock()
            whenever(activityMock.window).thenReturn(windowMock)
            whenever(windowMock.statusBarColor).thenReturn(statusBarColor)
            mockkStatic("com.glia.widgets.helper.ContextExtensionsKt")
            every { any<Context>().requireActivity() } returns activityMock
            every { any<Context>().asActivity() } returns activityMock
            mockkStatic("com.glia.widgets.helper.InsetsKt")
            every { any<WindowInsetsControllerCompat>().hideKeyboard() } returns Unit

            callControllerMock = mock()
            floatingVisitorVideoController = mock()
            controllerFactoryMock = mock()
            whenever(controllerFactoryMock.callController).thenReturn(callControllerMock)
            whenever(controllerFactoryMock.floatingVisitorVideoController).thenReturn(floatingVisitorVideoController)

            val rp = ResourceProvider(snapshotContent.context)
            val sp: StringProvider = SnapshotStringProvider(snapshotContent.context)
            Dependencies.setControllerFactory(controllerFactoryMock)
            Dependencies.setResourceProvider(rp)
            Dependencies.setStringProvider(sp)

            val schedulers = mock<Schedulers>()
            whenever(schedulers.computationScheduler) doReturn computationScheduler
            whenever(schedulers.mainScheduler) doReturn mainScheduler
            Dependencies.setSchedulers(schedulers)

            Dependencies.getSdkConfigurationManager().companyName = "SnapshotCall Tests"
        }

        fun tearDown() {
            Dependencies.getGliaThemeManager().theme = null
            Dependencies.setSdkConfigurationManager(GliaSdkConfigurationManager())
        }
    }

    data class ViewData(
        val root: View,
        val callView: CallView
    )

    fun setupView(
        callState: CallState? = null,
        executor: Executor? = Executor(Runnable::run),
        unifiedTheme: UnifiedTheme? = null,
        uiTheme: UiTheme? = null,
        callViewCallback: ((CallContract.View, callState: CallState?) -> Unit)? = null,
        floatingVisitorVideoViewCallback: ((FloatingVisitorVideoContract.View, callState: CallState?) -> Unit)? = null
    ): ViewData {
        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }

        val callViewCaptor: KArgumentCaptor<CallContract.View> = argumentCaptor()
        val floatingVisitorVideoViewCaptor: KArgumentCaptor<FloatingVisitorVideoContract.View> = argumentCaptor()

        val callActivityBinding = CallActivityBinding.inflate(layoutInflater)
        val root = callActivityBinding.root
        val callView = callActivityBinding.callView
        verify(callViewMock.callControllerMock).setView(callViewCaptor.capture())
        verify(callViewMock.floatingVisitorVideoController).setView(floatingVisitorVideoViewCaptor.capture())

        callView.setUiTheme(uiTheme)

        callView.executor = executor

        val callViewContract = callViewCaptor.lastValue
        callState?.let { callViewContract.emitState(it) }
        callViewCallback?.invoke(callViewContract, callState)

        floatingVisitorVideoViewCallback?.let {
            it(floatingVisitorVideoViewCaptor.lastValue, callState)
        }

        return ViewData(root, callView)
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
