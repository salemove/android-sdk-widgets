package com.glia.widgets.view.head

import android.app.Activity
import android.graphics.PointF
import android.mock
import android.os.Looper
import android.unMock
import android.view.View
import android.view.ViewGroup
import com.glia.widgets.R
import com.glia.widgets.chat.Intention
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.di.GliaCoreImpl
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.helper.hasChildOfType
import com.glia.widgets.helper.rootView
import com.glia.widgets.internal.chathead.BubbleContextEvent
import com.glia.widgets.internal.chathead.BubblePosition
import com.glia.widgets.internal.chathead.BubbleRenderTarget
import com.glia.widgets.internal.chathead.FakeChatBubbleController
import com.glia.widgets.internal.chathead.drag
import com.glia.widgets.internal.chathead.VisibleScreen
import com.glia.widgets.internal.chathead.domain.ResolveChatHeadNavigationUseCase.Destinations
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.StringKeyPair
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class ActivityWatcherForChatHeadTest {

    private lateinit var controller: FakeChatBubbleController
    private lateinit var activityLauncher: ActivityLauncher
    private lateinit var watcher: ActivityWatcherForChatHead
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        Logger.mock()
        val application = RuntimeEnvironment.getApplication()
        Dependencies.resourceProvider = ResourceProvider(application)

        val localeProvider = mock<LocaleProvider>()
        whenever(localeProvider.getLocaleObservable()) doReturn Observable.never()
        whenever(localeProvider.getString(any<Int>(), any<List<StringKeyPair>>())) doReturn "Back to the Engagement."
        Dependencies.localeProvider = localeProvider

        controller = FakeChatBubbleController()
        val controllerFactory = mock<ControllerFactory>()
        whenever(controllerFactory.chatBubbleController) doReturn controller
        Dependencies.controllerFactory = controllerFactory

        // GliaWidgets.isInitialized() reads Dependencies.gliaCore
        Dependencies.gliaCore = mockk { every { isInitialized } returns true }

        activityLauncher = mockk(relaxed = true)
        watcher = ActivityWatcherForChatHead(controller, activityLauncher)
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    }

    @After
    fun tearDown() {
        Logger.unMock()
        Dependencies.gliaCore = GliaCoreImpl()
    }

    private val contentView: ViewGroup get() = activity.rootView as ViewGroup

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private val hasBubble: Boolean get() = contentView.hasChildOfType(ChatHeadLayout::class.java)

    // region Lifecycle reporting

    @Test
    fun `onActivityResumed reports the resumed screen`() {
        // When
        watcher.onActivityResumed(activity)

        // Then
        assertEquals(listOf(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER)), controller.contextEvents)
    }

    @Test
    fun `onActivityPaused reports the screen that is on top afterwards`() {
        // Given two screens are resumed, as happens under the translucent call screen
        watcher.onActivityResumed(activity)
        val topActivity = Robolectric.buildActivity(Activity::class.java).setup().get()
        watcher.onActivityResumed(topActivity)
        controller.contextEvents.clear()

        // When the top one pauses, the one underneath is on top again
        watcher.onActivityPaused(topActivity)

        // Then
        assertEquals(listOf(BubbleContextEvent.ScreenChanged(VisibleScreen.OTHER)), controller.contextEvents)
    }

    // endregion

    // region Attaching the in-app bubble

    @Test
    fun `bubble is attached when the controller targets the in app bubble`() {
        // Given
        watcher.onActivityResumed(activity)

        // When
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // Then
        assertTrue(hasBubble)
    }

    @Test
    fun `bubble is not attached while the controller targets the overlay bubble`() {
        // Given
        watcher.onActivityResumed(activity)

        // When
        controller.emit(target = BubbleRenderTarget.SERVICE)
        idle()

        // Then
        assertFalse(hasBubble)
    }

    @Test
    fun `bubble is not attached before the SDK is initialized`() {
        // Given the watcher is registered before GliaWidgets.init has run
        Dependencies.gliaCore = mockk { every { isInitialized } returns false }
        watcher.onActivityResumed(activity)

        // When
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // Then
        assertFalse(hasBubble)
    }

    @Test
    fun `bubble is not attached while no activity is resumed`() {
        // When
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // Then
        assertFalse(hasBubble)
    }

    @Test
    fun `bubble is attached only once for repeated in app states`() {
        // Given
        watcher.onActivityResumed(activity)

        // When
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()
        controller.emit(target = BubbleRenderTarget.APPLICATION, unreadCount = 3)
        idle()

        // Then
        assertEquals(1, contentView.children().count { it is ChatHeadLayout })
    }

    // endregion

    // region Detaching the in-app bubble

    @Test
    fun `bubble is removed when the controller stops targeting the in app bubble`() {
        // Given
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // When
        controller.emit(target = BubbleRenderTarget.NONE)
        idle()

        // Then
        assertFalse(hasBubble)
    }

    @Test
    fun `bubble is removed when the resumed activity pauses`() {
        // Given
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // When
        watcher.onActivityPaused(activity)
        idle()

        // Then
        assertFalse(hasBubble)
    }

    @Test
    fun `bubble stays when an activity that is not resumed pauses`() {
        // Given MOB-3516: API 29 emulators deliver onActivityPaused after onActivityResumed
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()
        val otherActivity = Robolectric.buildActivity(Activity::class.java).setup().get()

        // When
        watcher.onActivityPaused(otherActivity)
        idle()

        // Then
        assertTrue(hasBubble)
    }

    // endregion

    // region Navigation

    @Test
    fun `tapping the bubble navigates to chat`() {
        // Given
        controller.tapDestination = Destinations.CHAT_VIEW
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // When
        bubble().findViewById<ChatHeadView>(R.id.chat_head_view).performClick()
        idle()

        // Then
        assertEquals(1, controller.tapCount)
        verify { activityLauncher.launchChat(activity, Intention.RETURN_TO_CHAT) }
    }

    @Test
    fun `tapping the bubble during a call finishes the chat screen it was tapped from`() {
        // Given
        controller.tapDestination = Destinations.CALL_VIEW
        controller.fromCallScreen = true
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // When
        bubble().findViewById<ChatHeadView>(R.id.chat_head_view).performClick()
        idle()

        // Then
        verify { activityLauncher.launchCall(activity, null, false) }
        assertEquals(1, controller.resetFromCallScreenCount)
        assertTrue(activity.isFinishing)
    }

    // endregion

    // region Surviving lifecycle events that don't change the state

    @Test
    fun `bubble comes back after the resumed activity pauses and resumes again`() {
        // Given the bubble state never changes across the pause, so StateFlow does not re-emit
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()
        watcher.onActivityPaused(activity)
        idle()

        watcher.onActivityResumed(activity)
        idle()

        assertTrue(hasBubble)
    }

    @Test
    fun `bubble follows navigation to another screen of the same kind`() {
        // Given a bubble on the first screen, and a model that does not change across the navigation
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()
        val next = Robolectric.buildActivity(Activity::class.java).setup().get()

        // When navigating: the outgoing activity pauses before the incoming one resumes
        watcher.onActivityPaused(activity)
        watcher.onActivityResumed(next)
        idle()

        // Then
        assertTrue((next.rootView as ViewGroup).hasChildOfType(ChatHeadLayout::class.java))
    }

    // endregion

    // region Bubble position

    @Test
    fun `dragging the bubble hands its position to the controller as a fraction`() {
        // Given
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // When the visitor drags the bubble all the way to its top-left limit
        chatHeadView().drag(dx = -10_000f, dy = -10_000f)

        // Then the shared position is the fraction of the drag range, ready for either host to map
        assertEquals(BubblePosition(0f, 0f), controller.bubblePosition)
    }

    @Test
    fun `bubble keeps the position it was dragged to across a pause and resume`() {
        // Given a bubble the visitor has dragged to its top-left limit
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()
        chatHeadView().drag(dx = -10_000f, dy = -10_000f)
        val draggedTo = PointF(chatHeadView().x, chatHeadView().y)

        // When the app goes to the background and comes back
        watcher.onActivityPaused(activity)
        idle()
        watcher.onActivityResumed(activity)
        idle()

        // Then the replacement bubble starts out where the old one was
        assertEquals(draggedTo, PointF(chatHeadView().x, chatHeadView().y))
    }

    @Test
    fun `an untouched bubble sits at the default corner`() {
        // When
        watcher.onActivityResumed(activity)
        controller.emit(target = BubbleRenderTarget.APPLICATION)
        idle()

        // Then it is at the bottom-right default, not at the (0, 0) a null position would map to
        assertTrue(chatHeadView().x > 0f)
        assertTrue(chatHeadView().y > 0f)
    }

    // endregion

    private fun bubble(): ChatHeadLayout =
        contentView.children().filterIsInstance<ChatHeadLayout>().first()

    private fun chatHeadView(): ChatHeadView = bubble().findViewById(R.id.chat_head_view)

    private fun ViewGroup.children(): List<View> = (0 until childCount).map(::getChildAt)
}
