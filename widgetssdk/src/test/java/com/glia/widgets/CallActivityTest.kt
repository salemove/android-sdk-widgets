package com.glia.widgets

import android.Manifest
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.glia.widgets.call.CallActivity
import com.glia.widgets.call.CallContract
import com.glia.widgets.call.CallStatus
import com.glia.widgets.call.testCallState
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.head.ChatHeadContract
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class CallActivityTest {
    private lateinit var appContext: Context
    private lateinit var controllerFactory: ControllerFactory
    private lateinit var callController: CallContract.Controller
    private lateinit var callView: CallContract.View
    private lateinit var serviceChatHeadController: ChatHeadContract.Controller
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var callStatus: CallStatus
    private lateinit var callViewSlot: CapturingSlot<CallContract.View>
    private lateinit var activityScenario: ActivityScenario<CallActivity>

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
        shadowOf(appContext.applicationContext as android.app.Application)
            .grantPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

        callViewSlot = slot<CallContract.View>()
        // set up ControllerFactory
        controllerFactory = mockk(relaxed = true)

        callController = mockk(relaxed = true)
        every { callController.shouldShowMediaEngagementView(any()) } returns true

        serviceChatHeadController = mockk(relaxed = true)

        every { controllerFactory.callController } answers { callController }

        every { controllerFactory.chatHeadController } answers { serviceChatHeadController }
        Dependencies.controllerFactory = controllerFactory

        Dependencies.repositoryFactory = mockk(relaxed = true)

        // set up ResourceProvider
        resourceProvider = ResourceProvider(appContext)
        Dependencies.resourceProvider = resourceProvider

        // set up StringProvider
        val localeProvider = LocaleProvider(resourceProvider)
        Dependencies.localeProvider = localeProvider

        callStatus = mockk(relaxed = true)
        every { callStatus.formattedOperatorName } answers { "FormattedOperatorName" }

        launchActivity()
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }

    private fun launchActivity() {
        activityScenario = ActivityScenario.launch(CallActivity::class.java)
        verify { callController.setView(capture(callViewSlot)) }
        callView = callViewSlot.captured
    }

    @Test
    fun testCallViewInvisible() {
        callView.emitState(testCallState(callStatus, isVisible = false))

        Espresso.onView(ViewMatchers.withId(R.id.call_view)).check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun testCallViewVisible() {
        callView.emitState(testCallState(callStatus, isVisible = true))

        Espresso.onView(ViewMatchers.withId(R.id.call_view)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testMinimizeButtonContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        val expected = appContext.getString(R.string.engagement_minimize_video_button)

        Espresso.onView(ViewMatchers.withId(R.id.minimize_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testSpeakerButtonOnContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, isSpeakerOn = true))
        val expected = appContext.getString(R.string.android_call_turn_speaker_off_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.speaker_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testSpeakerButtonOffContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, isSpeakerOn = false))
        val expected = appContext.getString(R.string.android_call_turn_speaker_on_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.speaker_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testMuteButtonContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, isMuted = true))
        val expected = appContext.getString(R.string.glia_call_mute_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.mute_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testUnmuteButtonContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, isMuted = false))
        val expected = appContext.getString(R.string.glia_call_unmute_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.mute_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testVideoButtonOnContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, hasVideo = true))
        val expected = appContext.getString(R.string.android_call_turn_video_off_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.video_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testVideoButtonOffContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, hasVideo = false))
        val expected = appContext.getString(R.string.android_call_turn_video_on_button_accessibility)
        Espresso.onView(ViewMatchers.withId(R.id.video_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testChatButtonZeroMessagesContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, messagesNotSeen = 0))
        val expected = appContext.getString(R.string.glia_call_chat_zero_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.chat_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testChatButtonPluralsMessagesContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true, messagesNotSeen = 15))
        val expected = appContext.resources.getString(
            R.string.glia_call_chat_other_content_description,
            15
        )
        Espresso.onView(ViewMatchers.withId(R.id.chat_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testConnectingViewContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        val expected = appContext.getString(
            R.string.engagement_connection_screen_connect_with,
            "FormattedOperatorName",
            ""
        )
        Espresso.onView(ViewMatchers.withId(R.id.connecting_view)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testVisitorVideoContainerContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        val expected = appContext.getString(R.string.call_visitor_video_accessibility_label)
        Espresso.onView(ViewMatchers.withId(R.id.visitor_video_card)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testOperatorVideoContainerContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        val expected = appContext.getString(R.string.call_operator_video_accessibility_label)
        Espresso.onView(ViewMatchers.withId(R.id.operator_video_container))
            .check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testEndButtonContentDescription() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        val expected = appContext.getString(R.string.glia_top_app_bar_chat_end_content_description)
        Espresso.onView(ViewMatchers.withId(R.id.end_button)).check(ViewAssertions.matches(ViewMatchers.withContentDescription(expected)))
    }

    @Test
    fun testNavigateUpButtonFinishesActivity() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        Espresso.onView(ViewMatchers.withContentDescription(R.string.android_app_bar_nav_up_accessibility)).perform(ViewActions.click())
        shadowOf(Looper.getMainLooper()).idle()
        var isFinishing = false
        activityScenario.onActivity { isFinishing = it.isFinishing }
        Assert.assertTrue(isFinishing)
    }

    @Test
    fun testOperatorNameHint() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        val expected = appContext.getString(R.string.glia_call_operator_name_hint)
        Espresso.onView(ViewMatchers.withId(R.id.operator_name_view)).check(ViewAssertions.matches(ViewMatchers.withHint(expected)))
    }

    @Test
    fun testCallDurationHint() {
        callView.emitState(testCallState(callStatus, isVisible = true))
        val expected = appContext.getString(R.string.call_duration_accessibility_label)
        Espresso.onView(ViewMatchers.withId(R.id.call_timer_view)).check(ViewAssertions.matches(ViewMatchers.withHint(expected)))
    }
}
