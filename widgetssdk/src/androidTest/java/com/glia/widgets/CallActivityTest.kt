package com.glia.widgets

import android.Manifest
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.CallContract
import com.glia.widgets.call.CallStateHelper
import com.glia.widgets.call.CallStatus
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.screensharing.ScreenSharingContract
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract
import com.glia.widgets.view.head.ChatHeadContract
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallActivityTest {
    private lateinit var appContext: Context
    private lateinit var controllerFactory: ControllerFactory
    private lateinit var callController: CallContract.Controller
    private lateinit var callView: CallContract.View
    private lateinit var serviceChatHeadController: ChatHeadContract.Controller
    private lateinit var sdkConfigurationManager: GliaSdkConfigurationManager
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var floatingVisitorVideoController: FloatingVisitorVideoContract.Controller
    private lateinit var callStatus: CallStatus
    private lateinit var callViewSlot: CapturingSlot<CallContract.View>
    private lateinit var activityScenario: ActivityScenario<CallActivity>

    @get:Rule
    var mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        callViewSlot = slot<CallContract.View>()
        // set up ControllerFactory
        controllerFactory = mockk(relaxed = true)

        callController = mockk(relaxed = true)
        every { callController.shouldShowMediaEngagementView(any()) } returns true

        serviceChatHeadController = mockk(relaxed = true)
        floatingVisitorVideoController = mockk(relaxed = true)

        every { controllerFactory.callController } answers { callController }

        every { controllerFactory.chatHeadController } answers { serviceChatHeadController }
        every { controllerFactory.floatingVisitorVideoController } answers { floatingVisitorVideoController }
        val screenSharingController: ScreenSharingContract.Controller = mockk(relaxed = true)
        every { controllerFactory.screenSharingController } returns screenSharingController
        Dependencies.setControllerFactory(controllerFactory)

        // set up SdkConfigurationManager
        sdkConfigurationManager = mockk(relaxed = true)
        every { sdkConfigurationManager.uiTheme } answers { UiTheme() }
        every { sdkConfigurationManager.companyName } answers { "Test Company" }
        Dependencies.setSdkConfigurationManager(sdkConfigurationManager)

        // set up ResourceProvider
        resourceProvider = ResourceProvider(appContext)
        Dependencies.setResourceProvider(resourceProvider)

        // set up StringProvider
        val stringProvider: StringProvider = AndroidTestStringProvider(appContext)
        Dependencies.setStringProvider(stringProvider)

        callStatus = mockk(relaxed = true)
        every { callStatus.formattedOperatorName } answers { "FormattedOperatorName" }

        launchActivity()
    }

    private fun launchActivity() {
        activityScenario = ActivityScenario.launch(CallActivity::class.java)
        verify { callController.setView(capture(callViewSlot)) }
        callView = callViewSlot.captured
    }

    @Test
    fun testCallViewInvisible() {
        val callState = CallStateHelper.Builder().setVisible(false).setCallStatus(callStatus).build()
        callView.emitState(callState.makeCallState())

        Espresso.onView(ViewMatchers.withId(R.id.call_view)).check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun testCallViewVisible() {
        val callState = CallStateHelper.Builder().setVisible(true).setCallStatus(callStatus).build()
        callView.emitState(callState.makeCallState())

        Espresso.onView(ViewMatchers.withId(R.id.call_view)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testMinimizeButtonContentDescription() {
        val callState = CallStateHelper.Builder().setVisible(true).setCallStatus(callStatus).build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.engagement_minimize_video_button)

        Espresso.onView(ViewMatchers.withId(R.id.minimize_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testSpeakerButtonOnContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setIsSpeakerOn(true)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.android_call_turn_speaker_off_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.speaker_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testSpeakerButtonOffContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setIsSpeakerOn(false)
            .build()
        callView.emitState(callState.makeCallState())
        val expected =
            appContext.getString(R.string.android_call_turn_speaker_on_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.speaker_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testMuteButtonContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setIsMuted(true)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.glia_call_mute_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.mute_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testUnmuteButtonContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setIsMuted(false)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.glia_call_unmute_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.mute_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testVideoButtonOnContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setHasVideo(true)
            .build()
        callView.emitState(callState.makeCallState())
        val expected =
            appContext.getString(R.string.android_call_turn_video_off_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.video_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testVideoButtonOffContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setHasVideo(false)
            .build()
        callView.emitState(callState.makeCallState())
        val expected =
            appContext.getString(R.string.android_call_turn_video_on_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.video_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testChatButtonZeroMessagesContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setMessagesNotSeen(0)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.glia_call_chat_zero_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.chat_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testChatButtonPluralsMessagesContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .setMessagesNotSeen(15)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.resources.getString(
            R.string.glia_call_chat_other_content_description,
            15
        )
        Espresso.onView(ViewMatchers.withId(R.id.chat_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testConnectingViewContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(
            R.string.engagement_connection_screen_connect_with,
            "FormattedOperatorName",
            ""
        )
        Espresso.onView(ViewMatchers.withId(R.id.connecting_view)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testVisitorVideoContainerContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.call_visitor_video_accessibility_label)
        Espresso.onView(ViewMatchers.withId(R.id.floating_visitor_video)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testOperatorVideoContainerContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.call_operator_video_accessibility_label)
        Espresso.onView(ViewMatchers.withId(R.id.operator_video_container))
            .check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testEndButtonContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.glia_top_app_bar_chat_end_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.end_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testNavigateUpButtonContentDescription() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .build()
        callView.emitState(callState.makeCallState())
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withContentDescription(R.string.android_app_bar_nav_up_accessibility)).perform(ViewActions.click())
        Assert.assertEquals(Lifecycle.State.DESTROYED, activityScenario.state)
    }

    @Test
    fun testOperatorNameHint() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.glia_call_operator_name_hint)
        Espresso.onView(ViewMatchers.withId(R.id.operator_name_view)).check(ViewAssertions.matches(ViewMatchers.withHint(expected)))
    }

    @Test
    fun testCallDurationHint() {
        val callState = CallStateHelper.Builder()
            .setVisible(true)
            .setCallStatus(callStatus)
            .build()
        callView.emitState(callState.makeCallState())
        val expected = appContext.getString(R.string.call_duration_accessibility_label)
        Espresso.onView(ViewMatchers.withId(R.id.call_timer_view)).check(ViewAssertions.matches(ViewMatchers.withHint(expected)))
    }
}
